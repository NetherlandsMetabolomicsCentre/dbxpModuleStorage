package org.dbxp.dbxpModuleStorage

class UploadedFileController {

    def uploadedFileService

    def index = { }

    def uploadFinished = {

        println 'Entered uploadFinished...'

        // render something to satisfy caller
        render ''

        // assume files are stored in '/tmp' for now
        uploadedFileService.handleUploadedFileWithPath("/tmp/$params.fileName")

    }
}
