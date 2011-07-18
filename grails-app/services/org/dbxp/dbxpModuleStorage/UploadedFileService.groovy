package org.dbxp.dbxpModuleStorage

import com.mongodb.gridfs.GridFS
import com.mongodb.gridfs.GridFSDBFile
import com.mongodb.gridfs.GridFSInputFile
import org.bson.types.ObjectId
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

    def handleUploadedFileWithPath(String path) {

        File uploadedFile = new File(path)

        if (uploadedFile.canRead()) {
            createUploadedFileFromFile(uploadedFile).save()
        }

    }

    /**
     * Reads a file and returns an instance of UploadedFile.
     *
     * @param file the file to load
     * @return an UploadedFile instance
     */
    UploadedFile createUploadedFileFromFile(File file) {

        GridFSInputFile gridFSInputFile = gridFS.createFile(file)
        gridFSInputFile.save()

        new UploadedFile(
                // TODO: set owner
                file_id:        gridFSInputFile.id.toString(),
                fileName:       gridFSInputFile.filename
        )

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

        def readableAssays                  = assayService.getAssaysReadableByUser(user)
        def readableAssaysWithUploadedFiles = readableAssays.findAll { AssayWithUploadedFile assay -> assay.uploadedFile_id }

        readableAssaysWithUploadedFiles.collect {
            AssayWithUploadedFile assay -> UploadedFile.get(assay.uploadedFile_id)
        }

    }

    List getUploadedFilesForUser(user) {

        Set uniqueUploadedFiles = (getFilesUploadedByUser(user) + getUploadedFilesFromAssaysReadableByUser(user))
        uniqueUploadedFiles as List

    }


}
