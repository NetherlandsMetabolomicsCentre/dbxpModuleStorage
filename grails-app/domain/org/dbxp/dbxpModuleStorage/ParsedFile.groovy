package org.dbxp.dbxpModuleStorage

class ParsedFile {

    def dateCreated
    def lastUpdated

    // a 2D matrix containing the parsed data as strings. Matrix[0] gives first
    // row, Matrix[0][0] give first cell of that row.
    def matrix

    // index of the row where the header is located
    Integer headerRowIndex = 0

    // TODO: should feature row be implemented? How to combine with header row?
//    Integer featureRowIndex = 0

    // index of the column containing the sample names
    Integer sampleColumn = 0

    // size of the matrix in rows/columns
    Integer rows = 0
    Integer columns = 0

    // a list of column indices that will not be exposed to the outside (e.g.
    // via REST calls)
    Integer[] ignoredDataColumns = []

    static constraints = {
    }

    static mapWith = "mongo"

}
