package org.dbxp.moduleStorage

class DataMatrixService {

    static transactional = 'mongo'

    /**
     * Imports a tab delimited file
     *
     * @param filePath path to the file to import
     * @return DataMatrix
     */
    DataMatrix importTabDelimitedFile(String filePath) {

        DataMatrix dataMatrix = new DataMatrix()
        def stringValues = []

        try {
            new File(filePath).eachLine { line ->

                stringValues << line.split('\t')

            }
        } catch (e) {
            throw new Exception("Could not read file: $filePath.")
        }

        // check whether all rows have equal length
        if ( (stringValues*.size().unique()).size() > 1) {
            throw new Exception("Error importing file: every row should have the same number of columns.")
        }

        dataMatrix.rows     = stringValues.size()
        dataMatrix.columns  = stringValues[0].size()

        // check whether dimensions are at least 2x2
        if (dataMatrix.rows < 2 || dataMatrix.columns < 2) {
            throw new Exception("Error importing file: file should have at least two rows and two columns. Rows: $dataMatrix.rows columns: $dataMatrix.columns.")
        }

        // try to convert values to double (or leave as strings) and assign
        // values to the data matrix
        dataMatrix.fileContents = stringValues.collect{ row ->

            row.collect { String stringValue ->
                stringValue.isDouble() ? stringValue.toDouble() : stringValue
            }
        }

        dataMatrix
    }

    /**
     *
     * @param dataMatrix
     * @param columns
     * @return
     */
    ArrayList getDataFromColumns(DataMatrix dataMatrix, ArrayList columns) {

        def transposedData = dataMatrix.fileContents.transpose()

        transposedData[columns].collect{ it[(dataMatrix.headerRowNumber + 1).. -1] }

    }

    /**
     *
     * @param dataMatrix
     * @param column
     * @return
     */
    ArrayList getDataFromColumn(DataMatrix dataMatrix, Integer column) {

        getDataFromColumns(dataMatrix, [column])[0] as ArrayList

    }

    /**
     *
     * @param dataMatrix
     * @return
     */
    ArrayList getHeaderRow(DataMatrix dataMatrix) {

        dataMatrix.fileContents[dataMatrix.headerRowNumber]

    }

    /**
     *
     * @param dataMatrix
     * @return
     */
    ArrayList getDataColumns(DataMatrix dataMatrix) {

        def dataColumns = (0..dataMatrix.columns-1)-dataMatrix.sampleColumn

        getDataFromColumns(dataMatrix, (ArrayList) dataColumns)

    }

}
