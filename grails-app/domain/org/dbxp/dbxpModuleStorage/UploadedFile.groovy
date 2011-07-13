package org.dbxp.dbxpModuleStorage

import org.dbxp.moduleBase.User

class UploadedFile {

    def dateCreated
    def lastUpdated

    User uploader // the 'owner' of the file. The keyword 'owner' is reserved in some database systems.

    // file name that was uploaded, stripped of path
    String name = ""

    // raw contents of uploaded file
    byte[] bytes = []

    // original filesize in bytes (equal to bytes.size)
    Long fileSize = 0

    /**
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

    // an associated assay
    AssayWithUploadedFile assay

    static constraints = {
        uploader    (nullable: true)
        parsedFile  (nullable: true)
        assay       (nullable: true)
    }

    static mapWith = "mongo"

}
