package org.dbxp.dbxpModuleStorage

import grails.converters.JSON

class RestController extends org.dbxp.moduleBase.RestController {

    def restService

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

        def measurements = restService.getMeasurements(params,response)

        render ((measurements ?: []) as JSON)
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
	 *
	 *
	 * @return  table (as hash) with values for given samples and measurements
	 *
	 *
	 * List of examples.
	 *
	 *
	 * Example REST call without sampleToken:
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
	 * The JSON object returned is an array of three arrays.
	 * The first nested array gives the sampleTokens for which data was retrieved.
	 * The second nested array gives the measurementToken for which data was retrieved.
	 * The third nested array gives the data for sampleTokens and measurementTokens.
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

        def measurementData = restService.getMeasurementData(params, response)

        render(measurementData as JSON)
    }
}
