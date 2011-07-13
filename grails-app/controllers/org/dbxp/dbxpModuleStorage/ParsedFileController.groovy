package org.dbxp.dbxpModuleStorage

class ParsedFileController {

    def uploadedFileService
    def parsedFileService

    def index = {

        render 'index, not implemented yet'

    }

    def uploadFinished = {

        // render something to satisfy caller
        render ''

        // assume files are stored in '/tmp' for now
        uploadedFileService.handleUploadedFileWithPath("/tmp/$params.fileName")

    }
}
