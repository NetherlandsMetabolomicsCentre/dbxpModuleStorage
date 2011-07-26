package org.dbxp.dbxpModuleStorage

import org.dbxp.moduleBase.Assay

class AssayWithUploadedFile extends Assay {

    // Store the id of the associated org.dbxp.dbxpModuleStorage.UploadedFile instance. Unfortunately a reference to
    // UploadedFile directly results in: org.hibernate.MappingException: An association from the table assay refers to
    // an unmapped class: UploadedFile.
    long uploadedFile_id

    /**
     * Shorthand method to overcome problem described above (see uploadedFile_id)
     * @return
     */
    UploadedFile getUploadedFile() {
        UploadedFile.get(uploadedFile_id)
    }

    def setUploadedFile(UploadedFile uploadedFile) {
        uploadedFile_id  = uploadedFile.id
    }

    static transients = ['uploadedFile']
}
