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

		def requestedSampleTokens		= params.sampleToken instanceof String ? [params.sampleToken] : params.sampleToken.toList()
        def sampleTokens                = getSampleTokensForSamplesWithData(uploadedFile, requestedSampleTokens)
        def requestedMeasurementTokens  = params.measurementToken instanceof String ? [params.measurementToken] : params.measurementToken
		def measurementTokens 			= uploadedFileService.getDataColumnHeaders(uploadedFile)

		if (requestedMeasurementTokens) {
        	measurementTokens           = measurementTokens.findAll { it in requestedMeasurementTokens }
		}

		def measurements = []

		if (measurementTokens) {
			measurements            	= uploadedFileService.getDataForSampleTokensAndMeasurementTokens(uploadedFile, sampleTokens, measurementTokens)?.transpose()?.flatten()
		}

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
			requestedSampleTokens.findAll { it in sampleTokens }
        else
            sampleTokens
    }
}
