package org.dbxp.dbxpModuleStorage

import com.mongodb.gridfs.GridFSDBFile
import org.dbxp.moduleBase.User

class UploadedFile {

    def uploadedFileService
    def parsedFileService

    static transients = ['uploadedFileService', 'parsedFileService', 'file', 'inputStream', 'byteArrayOutputStream', 'bytes']

    Date dateCreated
    Date lastUpdated

    User uploader // the 'owner' of the file. The keyword 'owner' is reserved in some database systems. TODO: change mapping instead

    // This is a hex string representing a Mongo ObjectId
    String file_id

    String fileName
    long fileSize

    /**
     * TODO: change to enum?
     * File type can be any of
     * '':                      default, meaning not yet determined
     * 'unknown':               not parsable, ie. when auto-detection failed
     * 'delimited_tab':         tab delimited
     * 'delimited_comma':       comma separated values, csv
     * 'delimited_semicolon':   european style csv
     * 'xls':                   pre-office 2007 excel spreadsheet
     * 'xlsx':                  Office Open XML
     *
     * more to come in the future
     */
    String fileType = ''

    // reference to org.dbxp.dbxpModuleStorage.ParsedFile instance, will be null
    // when fileType == '' or fileType == 'unknown'
    ParsedFile parsedFile

    AssayWithUploadedFile assay

    static constraints = {
        uploader    (nullable: true)
        parsedFile  (nullable: true)
        assay       (nullable: true)
    }

    GridFSDBFile getFile() {
        uploadedFileService.getGridFSDBFileByID(file_id)
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

    ParsedFile parse(Map hints = [:]) {
        parsedFile = parsedFileService.parseUploadedFile(this, hints)
    }

    static mapWith = "mongo"

}
