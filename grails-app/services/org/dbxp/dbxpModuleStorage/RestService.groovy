package org.dbxp.dbxpModuleStorage

import org.dbxp.moduleBase.Sample
import org.dbxp.moduleBase.Assay

class RestService {

    static transactional = false

    def parsedFileService
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
//        def assay = Assay.findByAssayToken(params.assayToken)

        if (!assay) {
            response.sendError(400, "No assay with token: \"$params.assayToken\" could be found.")
            return
        }

        assay
    }

    /**
     * Tries to get the parsed file for the assay requested via params.assayToken. Sends a 400 (Bad Request) if it can't
     * be found.
     *
     * @param params the params map containing the assayToken
     * @param response the response used to send an error code if needed
     * @return The requested assay
     */
    def ParsedFile getParsedFileOrSendError(assay, response) {

        ParsedFile parsedFile = UploadedFile.findByAssay(assay)?.parsedFile

        if (!parsedFile) {
            response.sendError(400, "Assay with token: \"$params.assayToken\" has no measurement data.")
            return
        }

        parsedFile
    }

    def ArrayList getMeasurements(params, response) {

        def parsedFile = getParsedFileOrSendError(params, response)

        parsedFileService.getFeatureNames parsedFile
    }

    def ArrayList getMeasurementData(params, response) {

        def assay = getAssayOrSendError(params, response)
        def parsedFile = getParsedFileOrSendError(assay, response)

        def sampleTokens                = getSampleTokensForSamplesWithData(parsedFile, params.sampleTokens)
        def requestedMeasurementTokens  = params.measurementToken instanceof String ? [params.measurementToken] : params.measurementToken
        def measurementTokens           = parsedFileService.getFeatureNames(parsedFile).findAll { it in requestedMeasurementTokens }
        def measurements                = parsedFileService.getDataForSamplesTokensAndMeasurementTokens(parsedFile, sampleTokens, measurementTokens).transpose().flatten()

        [sampleTokens, measurementTokens, measurements]
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
    ArrayList getSampleTokensForSamplesWithData(ParsedFile parsedFile, ArrayList requestedSampleTokens = null) {

        def sampleNames = parsedFileService.getSampleNames(parsedFile)
        def sampleTokens = Sample.findAllByNameInList(sampleNames)*.sampleToken

        if (requestedSampleTokens)
            sampleTokens.findAll { it in requestedSampleTokens }
        else
            sampleTokens
    }
}
