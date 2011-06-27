package org.dbxp.moduleStorage

class DataMatrixController {

    def importService

    def index = {

        def matrix1 = importService.importTabDelimitedFile('testData/DiogenesMockData.txt')

        matrix1.save(failOnError: true, flush: true)

        println matrix1.fileContents

        render matrix1.fileContents

    }
}
