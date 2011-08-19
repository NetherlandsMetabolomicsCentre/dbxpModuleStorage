package org.dbxp.dbxpModuleStorage

class ParsedFile {

    def dateCreated
    def lastUpdated

    static belongsTo = [uploadedFile: UploadedFile]

    // a 2D matrix containing the parsed data as strings. Matrix[0] gives first
    // row, Matrix[0][0] give first cell of that row.
    def matrix

    Integer featureRowIndex = 0

    // index of the column containing the sample names
    Integer sampleColumnIndex = 0

    // size of the matrix in rows/columns
    Integer rows = 0
    Integer columns = 0

    // a list of column indices that will not be exposed to the outside (e.g.
    // via REST calls)
    Integer[] ignoredDataColumns = []

    Map parseInfo

    Boolean isColumnOriented = false

    static mapWith = "mongo"
}
