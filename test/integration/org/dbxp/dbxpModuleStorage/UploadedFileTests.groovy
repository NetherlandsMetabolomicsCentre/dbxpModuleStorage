package org.dbxp.dbxpModuleStorage

class UploadedFileTests extends GroovyTestCase {

    def parsedFileService
    def uploadedFileService

    String fileName = 'testData/DiogenesMockData_mini.txt'

    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testImport() {

        def file = new File(fileName)
        def uploadedFile = uploadedFileService.createUploadedFileFromFile(file)

        assert uploadedFile

        assert uploadedFile.parse([delimiter: '\t', fileName: fileName])

        // flush because otherwise the data will not be persisted because this
        // is run inside a test transaction which is rolled back afterwards
        uploadedFile.save(flush: true, failOnError: true)

    }

    void testQueries() {

        // you can easily query mongofied entities by a fixed property name
        def uploadedFile = UploadedFile.findByFileName(fileName)

        assert uploadedFile

        def someData = parsedFileService.getDataFromColumn(uploadedFile.parsedFile, 1)

        println "We've got some data: $someData"

    }

}
