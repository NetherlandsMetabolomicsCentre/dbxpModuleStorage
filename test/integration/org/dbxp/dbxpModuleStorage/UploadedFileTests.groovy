package org.dbxp.dbxpModuleStorage

class UploadedFileTests extends GroovyTestCase {

    def parsedFileService
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
            uploadedFile = uploadedFileService.createUploadedFileFromFile(new File(filePath))
        }

        assert uploadedFile

        println UploadedFile.count()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testImport() {

        uploadedFile.parsedFile?.delete()

//        assert uploadedFile.parse([delimiter: '\t']) // won't work because of mongo services bug

        uploadedFile.parsedFile = parsedFileService.parseUploadedFile(uploadedFile, [delimiter: '\t'])

        uploadedFile.save(failOnError: true, flush: true)

        assert uploadedFile.parsedFile

    }

    void testParsedDataIsStored() {

        assert uploadedFile.parsedFile

        def someData = parsedFileService.getDataFromColumn(uploadedFile.parsedFile, 1)

        assert someData == ['803','89','523']

    }

    void testDelete() {

        if (!uploadedFile.parsedFile) testImport()

        def parsedFileId = uploadedFile.parsedFile.id

        uploadedFile.delete(flush: true)

        assert !ParsedFile.get(parsedFileId)


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
