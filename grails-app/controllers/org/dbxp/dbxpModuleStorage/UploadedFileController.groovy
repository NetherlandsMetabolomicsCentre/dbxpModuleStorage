package org.dbxp.dbxpModuleStorage

import com.mongodb.gridfs.GridFSFile
import grails.converters.JSON

class UploadedFileController {
	static final int BUFF_SIZE = 100000;
	static final byte[] buffer = new byte[BUFF_SIZE];

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

            uploadedFileService.deleteUploadedFile(uploadedFile)
            deletionWasSuccessful = true
            
        }

        render ([status: deletionWasSuccessful] as JSON)

    }

    def downloadUploadedFile = {
        UploadedFile uploadedFile = UploadedFile.get(params.fileId)
        if (!uploadedFile) response.sendError(404, "No uploaded file could be found matching id: ${params.fileId}.")

        GridFSFile gridFSFile = uploadedFileService.getGridFSDBFileByID(uploadedFile.gridFSFile_id)
        if (!gridFSFile) response.sendError(404, "No file attached to UploadedFile")

		response.setStatus(200)
		response.setHeader("Content-disposition", "attachment; filename=\"${uploadedFile.fileName}\"")
		response.setContentType("application/octet-stream")
		response.setContentLength(uploadedFile.fileSize as int)

		// define input and output streams
		InputStream inStream = null
		OutputStream outStream = null

		// handle file download
		try {
			inStream = gridFSFile.inputStream
			outStream = response.getOutputStream()

			while (true) {
				synchronized (buffer) {
					int amountRead = inStream.read(buffer)
					if (amountRead == -1) {
						break
					}
					outStream.write(buffer, 0, amountRead)
				}
				outStream.flush()
			}
		} catch (Exception e) {
			// whoops, looks like something went wrong
			println "download failed! ${e.getMessage()}"
		} finally {
			if (inStream != null) inStream.close()
			if (outStream != null) outStream.close()
		}

		return false
    }
}
