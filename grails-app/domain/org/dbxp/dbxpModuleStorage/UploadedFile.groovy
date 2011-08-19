package org.dbxp.dbxpModuleStorage

import com.mongodb.gridfs.GridFSDBFile
import org.dbxp.moduleBase.Assay
import org.dbxp.moduleBase.User

class UploadedFile {

    def uploadedFileService
    def parsedFileService

    static transients = ['uploadedFileService', 'parsedFileService', 'file', 'inputStream', 'byteArrayOutputStream', 'bytes']
    static mapWith = 'mongo'

    static hasOne = [parsedFile: ParsedFile]

    Date dateCreated
    Date lastUpdated

    User uploader // user who uploaded the file

    // This is a hex string representing a Mongo ObjectId
    String gridFSFile_id

    String fileName
    Long fileSize

    Assay assay

    static constraints = {
        uploader    (nullable: true)
        parsedFile  (nullable: true)
        assay       (nullable: true)
    }

    GridFSDBFile getFile() {
        uploadedFileService.getGridFSDBFileByID(gridFSFile_id)
    }

    InputStream getInputStream() {
        file?.inputStream
    }

    ByteArrayOutputStream getByteArrayOutputStream() {
        new ByteArrayOutputStream((int) fileSize) << file?.inputStream
    }

    byte[] getBytes() {
        byteArrayOutputStream?.toByteArray()
    }

    void parse(Map hints = [:]) {
        this.parsedFile = parsedFileService.parseUploadedFile(this, hints)
        this.parsedFile.uploadedFile = this
        parsedFile.save(failOnError: true)
    }

    void delete() {
        uploadedFileService.deleteUploadedFile(this)
    }
}
