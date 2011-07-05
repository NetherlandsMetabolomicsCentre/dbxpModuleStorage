package org.dbxp.pocModule

class UploadedFileTests extends GroovyTestCase {

    def parsedFileService

    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testImport() {

        def t = new Date()

        def matrix = parsedFileService.importTabDelimitedFile('testData/DiogenesMockData.txt')

        println 'Loading file took ' + (new Date().time - t.time)/1000 + ' seconds.'

        t = new Date()

        // insert a dynamic property for query testing purposes
        matrix['someDynamicProperty'] = true


        matrix.save(flush: true) // flush because otherwise the data will not be persisted because this is run inside a test transaction which is rolled back afterwards

        println 'Saving matrix took ' + (new Date().time - t.time)/1000 + ' seconds.'

//        t = new Date()
//
//        // assume sample names are in first column
//        def sampleNames = dataMatrixService.getDataFromColumn(matrix, 0)
//
//        // make some samples based on the sample names from the file
//        def samples = sampleNames.collect { sampleName ->
//            new Sample(name: sampleName)
//        }
//
//        // get all header cells excluding the first which belongs to sample
//        def dataColumnHeaders = dataMatrixService.getHeaderRow(matrix)[1..-1]
//
//        // get all data cells excluding those from the 'sample' column
//        def dataColumns = dataMatrixService.getDataColumns(matrix)
//
//        println 'Creating samples + retreiving data took ' + (new Date().time - t.time)/1000 + ' seconds.'
//
//        t = new Date()
//
//        samples.eachWithIndex { sample, sampleIndex ->
//
//            dataColumnHeaders.eachWithIndex { columnHeader, columnIndex ->
//
//                if (columnHeader)
//                    sample[columnHeader] = dataColumns[columnIndex][sampleIndex]
//
//            }
//
//            sample.save(flush: true)
//        }
//
//        println 'Adding data to samples and saving took ' + (new Date().time - t.time)/1000 + ' seconds.'
    }

    void testQueries() {

        // you can easily query mongofied entities by a fixed property name
        def matrix = UploadedFile.findByUploadedFileName('testData/DiogenesMockData.txt')

        assert matrix

        def matrix2

        // these dynamic finders even work for dynamic properties!
        matrix2 = UploadedFile.findBySomeDynamicProperty(true)

        assert matrix2

        assert matrix.id == matrix2.id

        def someData = parsedFileService.getDataFromColumn(matrix, 1)

        println "We've got some data: $someData"
        println "The sum is: ${someData*.toDouble().sum()}"



    }

    void testLoad() {

        def matrix = UploadedFile.get(1)

        println matrix.fileContents

    }

}
