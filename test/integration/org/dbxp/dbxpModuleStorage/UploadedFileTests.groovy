package org.dbxp.dbxpModuleStorage

class UploadedFileTests extends GroovyTestCase {

    def uploadedFileService

    String fileName = 'DiogenesMockData_mini.txt'
    String filePath = 'testData/' + fileName

    static transactional = 'mongo'

    UploadedFile uploadedFile

    protected void setUp() {
        super.setUp()

        uploadedFile = UploadedFile.findByFileName(fileName)
        if (!uploadedFile) {
            println 'creating new UploadedFile'
            uploadedFile = uploadedFileService.createUploadedFileFromFile(new File(filePath), null)
        }

        assert uploadedFile

        println UploadedFile.count()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testImport() {

        assert uploadedFile.parse([delimiter: '\t'])

        uploadedFile.save(failOnError: true, flush: true)

        assert uploadedFile.matrix

    }

    void testParsedDataIsStored() {

        assert uploadedFile.matrix

        def someData = uploadedFileService.getDataFromColumn(uploadedFile, 1)

        assert someData == ['803','89','523']

    }

    void testMongoDynamicProperty() {

        def myID = 'mySuperDuperID'

        uploadedFile['platformVersionID'] = myID

        assert uploadedFile['platformVersionID'] == myID

        uploadedFile.save(failOnError: true)

        def uploadedFile2 = UploadedFile.findByPlatformVersionID(myID)

        assert uploadedFile2

    }

}
