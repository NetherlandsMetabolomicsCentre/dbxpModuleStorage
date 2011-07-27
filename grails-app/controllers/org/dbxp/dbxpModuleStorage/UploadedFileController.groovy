package org.dbxp.dbxpModuleStorage

import com.mongodb.gridfs.GridFSFile
import grails.converters.JSON

class UploadedFileController {

    def uploadedFileService

    def index = { }

    def uploadFinished = {

        // assume files are stored in '/tmp' for now
        def uploadedFile = uploadedFileService.handleUploadedFileWithPath("/tmp/$params.fileName", session.user)

        // render info about stored uploaded file
        render([fileName: uploadedFile.fileName, fileSize: uploadedFile.fileSize, fileId: uploadedFile.id] as JSON)

    }

    def deleteUploadedFile = {

        def deletionWasSuccessful = false

        def uploadedFile = UploadedFile.get(params.fileId)

        // TODO: figure out who and when may delete what
        if (uploadedFile.uploader.id == session.user.id) {
            uploadedFile.delete()
            deletionWasSuccessful = true
        }

        render ([status: deletionWasSuccessful] as JSON)

    }

    def downloadUploadedFile = {

        UploadedFile uploadedFile = UploadedFile.get(params.fileId)
        if (!uploadedFile) response.sendError(404, "No uploaded file could be found matching id: ${params.fileId}.")

        GridFSFile gridFSFile = uploadedFileService.getGridFSDBFileByID(uploadedFile.gridFSFile_id)
        if (!gridFSFile) response.sendError(404, "No file attached to UploadedFile")

        response.setHeader("Content-disposition", "attachment;filename=\"${uploadedFile.fileName}\"")
		response.setContentType("application/octet-stream")
        response.setContentLength(uploadedFile.fileSize as int)
        response.outputStream << gridFSFile.inputStream
        response.outputStream.close()
    }
}
