package org.dbxp.pocModule

import org.dbxp.matriximporter.MatrixImporter

class ParsedFileService {

    static transactional = 'mongo'

    /**
     * Imports a tab delimited file
     *
     * @param filePath path to the file to import
     * @return ParsedFile
     */
    ParsedFile parseUploadedFile(UploadedFile uploadedFile, Map hints = [:]) {

        def matrix = MatrixImporter.instance.importByteArray(uploadedFile.bytes, hints)

        // check whether all rows have equal length
        if ( (matrix*.size().unique()).size > 1) {
            throw new Exception("Error importing file: every row should have the same number of columns.")
        }

        if (!matrix) return null

        ParsedFile parsedFile = new ParsedFile(
                matrix:     matrix,
                rows:       matrix.size,
                columns:    matrix[0].size()
        )

        // check whether dimensions are at least 2x2
        if (parsedFile.rows < 2 || parsedFile.columns < 2) {
            throw new Exception("Error importing file: file should have at least two rows and two columns. Rows: $parsedFile.rows columns: $parsedFile.columns.")
        }

        parsedFile
    }

    /**
     *
     * @param uploadedFile
     * @param columns
     * @return
     */
    ArrayList getDataFromColumns(ParsedFile parsedFile, ArrayList columns) {

        // TODO: check whether this can be more efficient
        def transposedData = parsedFile.matrix.transpose()

        transposedData[columns].collect{ it[(parsedFile.headerRowIndex + 1).. -1] }

    }

    /**
     *
     * @param uploadedFile
     * @param column
     * @return
     */
    ArrayList getDataFromColumn(ParsedFile parsedFile, Integer column) {

        getDataFromColumns(parsedFile, [column])[0] as ArrayList

    }

    /**
     *
     * @param uploadedFile
     * @return
     */
    ArrayList getHeaderRow(ParsedFile parsedFile) {

        parsedFile.matrix[parsedFile.headerRowIndex]

    }

    /**
     *
     * @param uploadedFile
     * @return
     */
    ArrayList getDataColumns(ParsedFile parsedFile) {

        def dataColumns = (0..parsedFile.columns-1)-parsedFile.sampleColumn

        getDataFromColumns(parsedFile, (ArrayList) dataColumns)

    }

}
