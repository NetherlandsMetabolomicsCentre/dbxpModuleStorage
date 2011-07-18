package org.dbxp.dbxpModuleStorage

import grails.converters.JSON

class RestController {

    def assayWithUploadedFileService
    def parsedFileService

    /**
     * Tries to get the assay requested via params.assayToken. Sends a 400 (Bad Request) if it can't be found.
     * @param params the params map containing the assayToken
     * @param response the response used to send an error code if needed
     * @return The requested assay
     */
    private AssayWithUploadedFile getAssay(params, response) {

        def assay = AssayWithUploadedFile.findByAssayToken(params.assayToken)

        if (!assay) {
            response.sendError(400, "No assay with token: \"$params.assayToken\" could be found.")
            return
        }

        assay

    }

	/**
	 * Return a list of simple assay measurements matching the querying text.
	 *
	 * @param assayToken
	 * @return list of measurements for token.
	 *
	 * Example REST call:
	 * http://localhost:8184/metagenomics/rest/getMeasurementNames/query?assayToken=16S-5162
	 *
	 * Resulting JSON object:
	 *
	 * [ "# sequences", "average quality" ]
	 *
	 */
	def getMeasurements = {

        def assay = getAssay(params, response)

        def measurements = assayWithUploadedFileService.getMeasurements(assay)

        render measurements as JSON

	}

	/**
	 * Return list of measurement data.
	 *
	 * @param assayToken
	 * @param measurementToken. Restrict the returned data to the measurementTokens specified here.
	 * 						If this argument is not given, all samples for the measurementTokens are returned.
	 * 						Multiple occurrences of this argument are possible.
	 * @param sampleToken. Restrict the returned data to the samples specified here.
	 * 						If this argument is not given, all samples for the measurementTokens are returned.
	 * 						Multiple occurrences of this argument are possible.
	 * @param boolean verbose. If this argument is not present or it's value is true, then return
	 *                      the date in a redundant format that is easier to process.
	 *						By default, return a more compact JSON object as follows.
	 *
	 * 						The list contains three elements:
	 *
	 *						(1) a list of sampleTokens,
	 *						(2) a list of measurementTokens,
	 * 						(3) a list of values.
	 *
	 * 						The list of values is a matrix represented as a list. Each row of the matrix
	 * 						contains the values of a measurementToken (in the order given in the measurement
	 * 						token list, (2)). Each column of the matrix contains the values for the sampleTokens
	 * 						(in the order given in the list of sampleTokens, (1)).
	 * 						(cf. example below.)
	 *
	 *
	 * @return  table (as hash) with values for given samples and measurements
	 *
	 *
	 * List of examples.
	 *
	 *
	 * Example REST call:
	 * http://localhost:8184/metagenomics/rest/getMeasurementData/doit?assayToken=PPSH-Glu-A
	 *    &measurementToken=total carbon dioxide (tCO)
	 *    &sampleToken=5_A
	 *    &sampleToken=1_A
	 *    &verbose=true
	 *
	 * Resulting JSON object:
	 * [ {"sampleToken":"1_A","measurementToken":"total carbon dioxide (tCO)","value":28},
	 *   {"sampleToken":"5_A","measurementToken":"total carbon dioxide (tCO)","value":29} ]
	 *
	 *
	 *
	 * Example REST call without sampleToken, without measurementToken,
	 *    and with verbose representation:
	 * http://localhost:8184/metagenomics/rest/getMeasurementData/dossit?assayToken=PPSH-Glu-A
	 *    &verbose=true
	 *
	 * Resulting JSON object:
	 * [ {"sampleToken":"1_A","measurementToken":"sodium (Na+)","value":139},
	 *	 {"sampleToken":"1_A","measurementToken":"potassium (K+)","value":4.5},
	 *	 {"sampleToken":"1_A","measurementToken":"total carbon dioxide (tCO)","value":26},
	 *	 {"sampleToken":"2_A","measurementToken":"sodium (Na+)","value":136},
	 *	 {"sampleToken":"2_A","measurementToken":"potassium (K+)","value":4.3},
	 *	 {"sampleToken":"2_A","measurementToken":"total carbon dioxide (tCO)","value":28},
	 *	 {"sampleToken":"3_A","measurementToken":"sodium (Na+)","value":139},
	 *	 {"sampleToken":"3_A","measurementToken":"potassium (K+)","value":4.6},
	 *	 {"sampleToken":"3_A","measurementToken":"total carbon dioxide (tCO)","value":27},
	 *	 {"sampleToken":"4_A","measurementToken":"sodium (Na+)","value":137},
	 *	 {"sampleToken":"4_A","measurementToken":"potassium (K+)","value":4.6},
	 *	 {"sampleToken":"4_A","measurementToken":"total carbon dioxide (tCO)","value":26},
	 *	 {"sampleToken":"5_A","measurementToken":"sodium (Na+)","value":133},
	 *	 {"sampleToken":"5_A","measurementToken":"potassium (K+)","value":4.5},
	 *	 {"sampleToken":"5_A","measurementToken":"total carbon dioxide (tCO)","value":29} ]
	 *
	 *
	 *
	 * Example REST call with default (non-verbose) view and without sampleToken:
	 *
	 * Resulting JSON object:
	 * http://localhost:8184/metagenomics/rest/getMeasurementData/query?
	 * 	assayToken=PPSH-Glu-A&
     * 	measurementToken=sodium (Na+)&
     * 	measurementToken=potassium (K+)&
	 *	measurementToken=total carbon dioxide (tCO)
	 *
	 * Resulting JSON object:
	 * [ ["1_A","2_A","3_A","4_A","5_A"],
	 *   ["sodium (Na+)","potassium (K+)","total carbon dioxide (tCO)"],
	 *   [139,136,139,137,133,4.5,4.3,4.6,4.6,4.5,26,28,27,26,29] ]
	 *
	 * Explanation:
	 * The JSON object returned by default (i.e., unless verbose is set) is an array of three arrays.
	 * The first nested array gives the sampleTokens for which data was retrieved.
	 * The second nested array gives the measurementToken for which data was retrieved.
	 * The thrid nested array gives the data for sampleTokens and measurementTokens.
	 *
	 *
	 * In the example, the matrix represents the values of the above Example and
	 * looks like this:
	 *
	 * 			1_A		2_A		3_A		4_A		5_A
	 *
	 * Na+		139		136		139		137		133
	 *
	 * K+ 		4.5		4.3		4.6		4.6		4.5
	 *
	 * tCO		26		28		27		26		29
	 *
	 */
	def getMeasurementData = {

        // TODO: either implement 'verbose' option or remove it from specs

        def assay = getAssay(params, response)
        ParsedFile parsedFile = assayWithUploadedFileService.getParsedFileFromAssay(assay)

        if (!parsedFile) {
            response.sendError(400, "Assay with token: \"$params.assayToken\" has no measurement data.")
            return
        }

        def sampleTokens                = assayWithUploadedFileService.getSampleTokensForSamplesWithDataFromAssay(assay, params.sampleTokens)
        def requestedMeasurementTokens  = params.measurementToken instanceof String ? [params.measurementToken] : params.measurementTokens
        def measurementTokens           = parsedFileService.getFeatureNames(parsedFile).findAll { it in requestedMeasurementTokens }
        def measurements                = parsedFileService.getDataForMeasurementTokens(parsedFile, measurementTokens).flatten()

		render [sampleTokens, measurementTokens, measurements] as JSON
	}
}
