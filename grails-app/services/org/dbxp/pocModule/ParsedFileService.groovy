package org.dbxp.pocModule

import org.dbxp.matriximporter.CsvReader

class ParsedFileService {

    static transactional = 'mongo'

    /**
     * Imports a tab delimited file
     *
     * @param filePath path to the file to import
     * @return ParsedFile
     */
    ParsedFile parseUploadedFile(UploadedFile uploadedFile) {

//        def matrix = MatrixImporter.instance.importFile(new ByteArrayInputStream(uploadedFile.bytes) as File)

        String s = new InputStreamReader(new ByteArrayInputStream (uploadedFile.bytes)).readLines().join('\n')

        def matrix = new CsvReader().parse(s, [:])

// This is made obsolete because MatrixImporter pads with empty strings
//        // check whether all rows have equal length
//        if ( (matrix*.size.unique()).size > 1) {
//            throw new Exception("Error importing file: every row should have the same number of columns.")
//        }

        ParsedFile parsedFile = new ParsedFile(
                matrix:     matrix,
                rows:       matrix.size,
                columns:    matrix[0].size
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
