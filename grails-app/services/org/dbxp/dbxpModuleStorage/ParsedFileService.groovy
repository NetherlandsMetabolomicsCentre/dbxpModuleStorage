package org.dbxp.dbxpModuleStorage

import org.dbxp.matriximporter.MatrixImporter

class ParsedFileService {

    static transactional = 'mongo'

    /**
     * Parses contents of an UploadedFile instance to a ParsedFile instance.
     * The method checks whether all rows are equally long and whether the dimensions exceed 2x2.
     *
     * @param uploadedFile
     * @param hints
     * @return
     */
    ParsedFile parseUploadedFile(UploadedFile uploadedFile, Map hints = [:]) {

        def matrix = MatrixImporter.instance.importInputStream(uploadedFile.inputStream, hints + [fileName: uploadedFile.fileName])

        // check whether all rows have equal length
        if ( (matrix*.size().unique()).size > 1) {
            throw new Exception("Error importing file: every row should have the same number of columnIndices.")
        }

        if (!matrix) return null

        ParsedFile parsedFile = new ParsedFile(
                matrix:     matrix,
                rows:       matrix.size,
                columns:    matrix[0].size()
        )

        // check whether dimensions are at least 2x2
        if (parsedFile.rows < 2 || parsedFile.columns < 2) {
            throw new Exception("Error importing file: file should have at least two rows and two columnIndices. Rows: $parsedFile.rows columnIndices: $parsedFile.columns.")
        }

        parsedFile
    }

    /**
     * Gets data from columns with specified columnIndices. Does not include header in result.
     *
     * @param parsedFile
     * @param columnIndices
     * @return
     */
    ArrayList getDataFromColumns(ParsedFile parsedFile, ArrayList columnIndices) {

        // TODO: check whether this can be more efficient
        def transposedData = parsedFile.matrix.transpose()

        transposedData[columnIndices].collect{ it[(parsedFile.headerRowIndex + 1).. -1] }

    }

    /**
     * Gets data from the column with specified columnIndex. Does not include header in result.
     *
     * @param parsedFile
     * @param columnIndex
     * @return
     */
    ArrayList getDataFromColumn(ParsedFile parsedFile, Integer columnIndex) {

        getDataFromColumns(parsedFile, [columnIndex])[0] as ArrayList

    }

    /**
     * Returns the contents of the header row of a parsed file.
     *
     * @param parsedFile
     * @return
     */
    ArrayList getHeaderRow(ParsedFile parsedFile) {

        parsedFile.matrix[parsedFile.headerRowIndex]

    }

    /**
     * Returns indices from columns that contain data. The sample column and indices from parsedFile.ignoredDataColumns
     * are omitted.
     *
     * @param parsedFile
     * @return
     */
    ArrayList getDataColumnIndices(ParsedFile parsedFile) {

        (0..parsedFile.columns - 1) - parsedFile.sampleColumn - parsedFile.ignoredDataColumns

    }

    /**
     * Returns measurement names from the header of the data matrix. The sample column and columns with indices from
     * parsedFile.ignoredDataColumns are not returned.
     *
     * @param parsedFile
     * @return
     */
    ArrayList getFeatureNames(ParsedFile parsedFile) {

        getHeaderRow(parsedFile)[getDataColumnIndices(parsedFile)]

    }

    /**
     * Returns data columns for given measurementTokens.
     *
     * @param parsedFile
     * @param measurementTokens
     * @return
     */
    ArrayList getDataForMeasurementTokens(ParsedFile parsedFile, measurementTokens) {

        ArrayList columnIndices = getHeaderRow(parsedFile).findIndexValues { it in measurementTokens }

        getDataFromColumns(parsedFile, columnIndices)

    }

    /**
     * Returns data from the columns that contain measurement data.
     *
     * @param parsedFile
     * @return
     */
    ArrayList getMeasurementData(ParsedFile parsedFile) {

        ArrayList dataColumns = getDataColumnIndices(parsedFile)

        getDataFromColumns(parsedFile, dataColumns)

    }

    /**
     * Returns sample names from the parsed file.
     * It does this by return the data for the column marked as 'sampleColumn'
     *
     * @param parsedFile
     * @return
     */
    ArrayList getSampleNames(ParsedFile parsedFile) {

        getDataFromColumn(parsedFile, parsedFile.sampleColumn)

    }

    /**
     * Retrieves row indices that belong to given sample names.
     *
     * @param parsedFile
     * @param sampleNames
     * @return
     */
    ArrayList getRowIndicesForSampleNames(ParsedFile parsedFile, sampleNames) {

        parsedFile.matrix.findIndexValues { row -> row[parsedFile.sampleColumn] in sampleNames }

    }

    /**
     * Returns data for specific samples
     *
     * @param parsedFile
     * @param sampleNames
     * @return
     */
    ArrayList getDataForSampleNames(ParsedFile parsedFile, sampleNames) {

        def rows = getRowIndicesForSampleNames(parsedFile, sampleNames)

        getMeasurementData(parsedFile)[rows]

    }

    /**
     * Returns data for specific samples and measurementTokens
     *
     * @param parsedFile
     * @param sampleNames
     * @param measurementTokens
     * @return
     */
    ArrayList getDataForSamplesNamesAndMeasurementTokens(ParsedFile parsedFile, sampleNames, measurementTokens) {

        def rows = getRowIndicesForSampleNames(parsedFile, sampleNames)

        getDataForMeasurementTokens(parsedFile, measurementTokens)[rows]

    }

}
