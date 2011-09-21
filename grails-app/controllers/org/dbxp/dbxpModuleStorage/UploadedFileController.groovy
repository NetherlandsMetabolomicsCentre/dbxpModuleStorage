package org.dbxp.dbxpModuleStorage

import com.mongodb.gridfs.GridFSFile
import grails.converters.JSON
import org.codehaus.groovy.grails.commons.ConfigurationHolder

class UploadedFileController {
	static final int BUFF_SIZE = 100000;
	static final byte[] buffer = new byte[BUFF_SIZE];

    def uploadedFileService

    def index = { }

    def uploadFinished = {
		def fileName = request.getHeader('X-File-Name')

        // assume files are stored in '/tmp' for now
        def uploadedFile = uploadedFileService.handleUploadedFileWithPath("/tmp/${fileName}", session.user)

        // render info about stored uploaded file
        render([fileName: uploadedFile.fileName, fileSize: uploadedFile.fileSize, fileId: uploadedFile.id] as JSON)
    }

    def deleteAllUploadedFilesForCurrentUser = {
		def files, count

		// make sure this functionality is only used in the grails
		// environment configured in Config.groovy
	    println ConfigurationHolder.config.development.bar
		println grails.util.GrailsUtil.environment
		if ((ConfigurationHolder.config.development.bar).contains(grails.util.GrailsUtil.environment)) {
			files = uploadedFileService.getUploadedFilesForUser(session.user)
			count = files.size()

			// iterate through files
			files.each { uploadedFile ->
				uploadedFile.delete()
			}

			render ([status: 200, message: "${count} files deleted"] as JSON)
		} else {
			response.sendError(405, "this functionality is not available")
		}
	}

	def deleteUploadedFile = {
		def fileName 	= request.getHeader('X-File-Name')
		def fileId 		= request.getHeader('X-File-Id')
		def uploadedFile= UploadedFile.get(fileId)

		// try to delete the file
		try {
			// check if user may delete this file
			if (uploadedFile.uploader.id == session.user.id || session.user.isAdministrator) {
				// user may delete
                uploadedFile.delete()

				render ([status: 200, message: "The file '${fileName}' was deleted"] as JSON)
			} else {
				// user is not allowed to delete file
				render ([status: 405, message: "The file '${fileName}' cannot be deleted as you are not the file owner, nor are you authorized to do so by the file's owner"] as JSON)
			}
		} catch (Exception e) {
			// something went wrong deleting the file :S
			render ([status: 500, message: "An error occurred while trying to delete '${fileName}' (${e.getMessage()}"] as JSON)
		}
    }

    def downloadUploadedFile = {
        UploadedFile uploadedFile = UploadedFile.get(params.fileId)
        if (!uploadedFile) response.sendError(404, "No uploaded file could be found matching id: ${params.fileId}.")

        GridFSFile gridFSFile = uploadedFile.file
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