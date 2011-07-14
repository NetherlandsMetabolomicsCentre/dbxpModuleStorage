package org.dbxp.dbxpModuleStorage

import org.dbxp.moduleBase.Sample

class AssayService {

    static transactional = true

    def parsedFileService

    /**
     * Returns measurements for a given assay
     * @param assay
     * @return an ArrayList of measurements
     */
    ArrayList getMeasurements(AssayWithUploadedFile assay) {

        ParsedFile parsedFile = getParsedFileFromAssay(assay)

        if (parsedFile) {

            parsedFileService.getMeasurementNames parsedFile

        } else []

    }

    /**
     * Return the ParsedFile instance associated with an assay or null if none exists
     *
     * @param assay
     * @return
     */
    ParsedFile getParsedFileFromAssay(AssayWithUploadedFile assay) {

        assay?.uploadedFile?.parsedFile

    }

    /**
     * Return sample tokens from an assay that have a corresponding sample name entry in the connected parsedFile
     * instance. If requestedSampleTokens is non empty, only those sampleTokens matching the requestedSampleTokens are
     * returned.
     *
     * @param assay
     * @param requestedSampleTokens
     * @return
     */
    ArrayList getSampleTokensForSamplesWithDataFromAssay(AssayWithUploadedFile assay, ArrayList requestedSampleTokens = null) {

        ParsedFile parsedFile = getParsedFileFromAssay(assay)

        def sampleNames = parsedFileService.getSampleNames(parsedFile)
        def sampleTokens = sampleNames.collect { String sampleName ->

            Sample.findByName(sampleName).sampleToken

        }

        if (requestedSampleTokens)
            sampleTokens.findAll { it in requestedSampleTokens }
        else
            sampleTokens

    }
}
