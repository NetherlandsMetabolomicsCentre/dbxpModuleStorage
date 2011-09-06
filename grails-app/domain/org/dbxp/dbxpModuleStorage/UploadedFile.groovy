package org.dbxp.dbxpModuleStorage

import com.mongodb.gridfs.GridFSDBFile
import org.dbxp.moduleBase.Assay
import org.dbxp.moduleBase.User

class UploadedFile {

    def uploadedFileService
    def parsedFileService

    static transients = ['uploadedFileService', 'parsedFileService', 'file', 'inputStream', 'byteArrayOutputStream', 'bytes']
    static mapWith = 'mongo'

    static hasOne = [parsedFile: ParsedFile]

    Date dateCreated
    Date lastUpdated

    User uploader // user who uploaded the file

    // This is a hex string representing a Mongo ObjectId
    String gridFSFile_id

    String fileName
    Long fileSize

    Assay assay

    static constraints = {
        uploader    (nullable: true)
        parsedFile  (nullable: true)
        assay       (nullable: true)
    }

    GridFSDBFile getFile() {
        uploadedFileService.getGridFSDBFileByID(gridFSFile_id)
    }

    InputStream getInputStream() {
        file?.inputStream
    }

    ByteArrayOutputStream getByteArrayOutputStream() {
        new ByteArrayOutputStream((int) fileSize) << file?.inputStream
    }

    byte[] getBytes() {
        byteArrayOutputStream?.toByteArray()
    }

    void parse(Map hints = [:]) {
        this.parsedFile = parsedFileService.parseUploadedFile(this, hints)
        this.parsedFile.uploadedFile = this
        parsedFile.save(failOnError: true)
    }

    void delete() {
        uploadedFileService.deleteUploadedFile(this)
    }

	/**
	 * Rating calculation:
	 * 0 = file is uploaded (exists)
	 * 1 = file is attached to a study
	 * 2 = file is attached to assays
	 * 3 = all samples are recognized
	 * 4 = all features are recognized
	 * 5 = study is public
	 */
	def getRating() {
		def maxPoints = 5
		def points = 0

		// 1 : file is attached to a study
		// 2 : file is attach to a(n) assay(s)
		points = (this.assay) ? 2 : 0

		// 3 : all samples are recognized
		points = (points == 2 && this.parsedFile && this.assay?.samples.count() == this.determineAmountOfSamplesWithData()) ? 3 : 2

		// 4 : all features are recognized
		// TODO

		// 5 : study is public
		// TODO

		// calculate and return rating
		return (points / maxPoints)
	}

	def determineAmountOfSamplesWithData() {
		def sampleNamesInFile = parsedFileService.getSampleNames(this?.parsedFile)
		def samplesInAssay = this?.assay ? this.assay.samples : []
		def sampleNamesInAssay = samplesInAssay*.name
		sampleNamesInAssay.intersect(sampleNamesInFile).size()
	}
}