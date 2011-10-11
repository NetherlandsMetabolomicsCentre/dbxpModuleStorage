package org.dbxp.dbxpModuleStorage

import org.dbxp.moduleBase.Assay
import org.dbxp.moduleBase.Sample

class RestService {

    static transactional = false

    def uploadedFileService
    def synchronizationService

    /**
     * Tries to get the assay requested via params.assayToken. Sends a 400 (Bad Request) if it can't be found.
     *
     * @param params the params map containing the assayToken
     * @param response the response used to send an error code if needed
     * @return The requested assay
     */
    def Assay getAssayOrSendError(params, response) {

        def assay = synchronizationService.determineClassFor('Assay').findByAssayToken(params.assayToken)

        if (!assay) {
            response.sendError(400, "No assay with token: \"$params.assayToken\" could be found.")
            return
        }

        assay
    }

    ArrayList getMeasurements(params, response) {

		def assay = getAssayOrSendError(params, response)

		if (!assay) return
        def uploadedFile = UploadedFile.findByAssay(assay)

		if (!uploadedFile) return []

        uploadedFileService.getDataColumnHeaders uploadedFile
    }

    ArrayList getMeasurementData(params, response) {

        def assay = getAssayOrSendError(params, response)
		if (!assay) return
        def uploadedFile = UploadedFile.findByAssay(assay)
		if (!uploadedFile) return []

		println "DEBUG INFO for getMeasurementData"
		println "assay:	$assay"
		println "uploaded file: $uploadedFile"


        def sampleTokens                = getSampleTokensForSamplesWithData(uploadedFile, params.sampleTokens)
		println "sampleTokens: $sampleTokens"
        def requestedMeasurementTokens  = params.measurementToken instanceof String ? [params.measurementToken] : params.measurementToken
		println "requestedMeasurementTokens: $requestedMeasurementTokens"
        def measurementTokens           = uploadedFileService.getDataColumnHeaders(uploadedFile).findAll { it in requestedMeasurementTokens }
		println "measurementTokens: $measurementTokens"
        def measurements                = uploadedFileService.getDataForSamplesTokensAndMeasurementTokens(uploadedFile, sampleTokens, measurementTokens).transpose().flatten()
		println "measurements: $measurements"

        [sampleTokens, measurementTokens, measurements]
    }

    /**
     * Return sample tokens from an assay that have a corresponding sample name entry in the connected uploadedFile
     * instance. If requestedSampleTokens is non empty, only those sampleTokens matching the requestedSampleTokens are
     * returned.
     *
     * @param assay
     * @param requestedSampleTokens
     * @return
     */
    ArrayList getSampleTokensForSamplesWithData(UploadedFile uploadedFile, ArrayList requestedSampleTokens = null) {

        def sampleNames = uploadedFileService.getSampleNames(uploadedFile)
        def sampleTokens = Sample.findAllByNameInList(sampleNames)*.sampleToken

        if (requestedSampleTokens)
            sampleTokens.findAll { it in requestedSampleTokens }
        else
            sampleTokens
    }
}
