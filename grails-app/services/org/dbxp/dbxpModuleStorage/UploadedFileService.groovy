package org.dbxp.dbxpModuleStorage

import com.mongodb.gridfs.GridFS
import com.mongodb.gridfs.GridFSDBFile
import com.mongodb.gridfs.GridFSInputFile
import org.bson.types.ObjectId
import org.dbxp.moduleBase.User
import org.springframework.beans.factory.InitializingBean

class UploadedFileService implements InitializingBean {

    static transactional = 'mongo'

    def assayService

    def grailsApplication
    def mongoDatastore

    GridFS gridFS

    /**
     * This will be called after the service has been initialized. We're initializing the
     * gridFS here.
     */
    void afterPropertiesSet() {

        def db = mongoDatastore.mongo.getDB(mongoDatastore.mappingContext.defaultDatabaseName)

        gridFS = new GridFS(db)
    }

    UploadedFile handleUploadedFileWithPath(String path, User user) {

        File file = new File(path)

        def uploadedFile = null

        if (file.canRead()) {
            uploadedFile = createUploadedFileFromFile(file, user).save()
        }

        file.delete()

        uploadedFile
    }

    /**
     * Reads a file and returns an instance of UploadedFile.
     *
     * @param file the file to load
     * @return an UploadedFile instance
     */
    UploadedFile createUploadedFileFromFile(File file, User user) {

        GridFSInputFile gridFSInputFile = gridFS.createFile(file)
        gridFSInputFile.save()

        def uploadedFile = new UploadedFile(
                uploader:       user,
                gridFSFile_id:  gridFSInputFile.id.toString(),
                fileName:       file.name,
                fileSize:       file.length()
        )

        uploadedFile.save(failOnError: true)

        uploadedFile
    }

    GridFSDBFile getGridFSDBFileByID(String objectIdString) {

        getGridFSDBFileByID(new ObjectId(objectIdString))
    }


    GridFSDBFile getGridFSDBFileByID(ObjectId objectId) {

        gridFS.findOne(objectId)
    }

    List getFilesUploadedByUser(user) {

        UploadedFile.findAllByUploader(user)
    }

    List getUploadedFilesFromAssaysReadableByUser(user) {

        UploadedFile.findAllByAssayInList(assayService.getAssaysReadableByUser(user))
    }

    List getUploadedFilesFromAssaysWritableByUser(user) {

        UploadedFile.findAllByAssayInList(assayService.getAssaysWritableByUser(user))
    }

    List getUploadedFilesForUser(user) {

        (getFilesUploadedByUser(user) + getUploadedFilesFromAssaysWritableByUser(user)).unique()
    }

    def deleteUploadedFile(UploadedFile uploadedFile) {

        gridFS.remove(new ObjectId(uploadedFile.gridFSFile_id))
        uploadedFile.parsedFile.delete()
        uploadedFile.delete()
    }
}
