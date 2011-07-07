package org.dbxp.pocModule

class UploadedFileTests extends GroovyTestCase {

    def parsedFileService
    def uploadedFileService

    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testImport() {

        def file            = new File('testData/DiogenesMockData_mini.txt')
        def uploadedFile    = uploadedFileService.createUploadedFileFromFile(file)

        assert uploadedFile

        def parsedFile      = parsedFileService.parseUploadedFile(uploadedFile, [delimiter: '\t'])

        assert parsedFile

        // flush because otherwise the data will not be persisted because this
        // is run inside a test transaction which is rolled back afterwards
        uploadedFile.save   (flush: true, failOnError: true)
        parsedFile.save     (flush: true, failOnError: true)

    }

    void testQueries() {

        // you can easily query mongofied entities by a fixed property name
        def uploadedFile = UploadedFile.findByName('DiogenesMockData_mini.txt')

        assert uploadedFile

        def someData = parsedFileService.getDataFromColumn(uploadedFile.parsedFile, 1)

        println "We've got some data: $someData"

    }

}
