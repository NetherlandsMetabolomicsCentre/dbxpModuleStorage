package org.dbxp.dbxpModuleStorage

class UploadedFileService {

    static transactional = 'mongo'

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
}
