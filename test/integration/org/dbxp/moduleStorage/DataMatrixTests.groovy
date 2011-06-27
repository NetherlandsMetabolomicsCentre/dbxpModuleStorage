package org.dbxp.moduleStorage

class DataMatrixTests extends GroovyTestCase {

    def dataMatrixService

    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testImport() {

        def t = new Date()

        def matrix = dataMatrixService.importTabDelimitedFile('testData/DiogenesMockData.txt')

        println 'Loading file took ' + (new Date().time - t.time)/1000 + ' seconds.'

        t = new Date()

        matrix.save()

        println 'Saving matrix took ' + (new Date().time - t.time)/1000 + ' seconds.'

        t = new Date()

        // assume sample names are in first column
        def sampleNames = dataMatrixService.getDataFromColumn(matrix, 0)

        // make some samples based on the sample names from the file
        def samples = sampleNames.collect { sampleName ->
            new Sample(name: sampleName)
        }

        // get all header cells excluding the first which belongs to sample
        def dataColumnHeaders = dataMatrixService.getHeaderRow(matrix)[1..-1]

        // get all data cells excluding those from the 'sample' column
        def dataColumns = dataMatrixService.getDataColumns(matrix)

        println 'Creating samples + retreiving data took ' + (new Date().time - t.time)/1000 + ' seconds.'

        t = new Date()

        samples.eachWithIndex { sample, sampleIndex ->

            dataColumnHeaders.eachWithIndex { columnHeader, columnIndex ->

                if (columnHeader)
                    sample[columnHeader] = dataColumns[columnIndex][sampleIndex]

            }

            sample.save(flush: true)
        }

        println 'Adding data to samples and saving took ' + (new Date().time - t.time)/1000 + ' seconds.'
    }

    void testLoad() {

        def matrix = DataMatrix.get(1)

        println matrix.fileContents

    }

}
