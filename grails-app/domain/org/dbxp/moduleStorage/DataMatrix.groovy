package org.dbxp.moduleStorage

class DataMatrix {

    def dateCreated
    def lastUpdated

    def fileContents = []
    def uploadedFileName = ""
    def headerRowNumber = 0
    def sampleColumn = 0
    def rows = 0
    def columns = 0

    static constraints = {
    }

    static mapWith = "mongo"
}
