package org.dbxp.dbxpModuleStorage

class UploadedFileService {

    static transactional = 'mongo'

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

        new UploadedFile(
                // TODO: set owner
                bytes:      file.bytes,
                name:       file.name,
                fileSize:   file.length(),
        )

    }

    ArrayList getUploadedFilesForUser(user) {

        // TODO: implement logic to return files uploaded by current user as well as uploaded files connected to readable assays, something like this:

//        UploadedFile.findAllByUploader(user)

//        // make this one query to find by user AND non-zero uploadFile_id at once
//        def readableAssaysWithUploadedFiles = assayService.getAssaysReadableByUser(user).findAll {it.uploadedFile_id}

//        readableAssaysWithUploadedFiles*.uploadFile

        // for now, simply return all uploaded files
        UploadedFile.all

    }
}
