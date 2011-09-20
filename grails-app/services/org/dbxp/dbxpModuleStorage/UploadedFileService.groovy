package org.dbxp.dbxpModuleStorage

import com.mongodb.gridfs.GridFS
import com.mongodb.gridfs.GridFSDBFile
import com.mongodb.gridfs.GridFSInputFile
import org.bson.types.ObjectId
import org.dbxp.moduleBase.User
import org.dbxp.matriximporter.MatrixImporter
import org.dbxp.moduleBase.Sample

class UploadedFileService {

	static transactional = 'mongo'

	def assayService

	def grailsApplication
	def mongoDatastore

	GridFS gridFS

	/**
	 * This will be called after the service has been initialized. We're initializing the
	 * gridFS here.
	 */
	synchronized GridFS getGridFS() {
		def db

		// already got a gridFS instance?
		if (!this.gridFS) {
			// no, instantiate it
			db = mongoDatastore.mongo.getDB(mongoDatastore.mappingContext.defaultDatabaseName)
			this.gridFS = new GridFS(db)
		}

		return this.gridFS
	}

	/**
	 * import file from disk into mongodb
	 * @param path
	 * @param user
	 * @return
	 */
	UploadedFile handleUploadedFileWithPath(String path, User user) {
		File file = new File(path)
		def uploadedFile = null

		if (file.canRead()) {
			uploadedFile = createUploadedFileFromFile(file, user).save()
		}

		file.delete()

		return uploadedFile
	}

	/**
	 * Reads a file and returns an instance of UploadedFile.
	 *
	 * @param file the file to load
	 * @return an UploadedFile instance
	 */
	UploadedFile createUploadedFileFromFile(File file, User user) {
		GridFSInputFile gridFSInputFile = this.getGridFS().createFile(file)
		gridFSInputFile.save()

		def uploadedFile = new UploadedFile(
			uploader: user,
			gridFSFile_id: gridFSInputFile.id.toString(),
			fileName: file.name,
			fileSize: file.length()
		)

		uploadedFile.save(failOnError: true)

		return uploadedFile
	}

	GridFSDBFile getGridFSDBFileByID(String objectIdString) {
		getGridFSDBFileByID(new ObjectId(objectIdString))
	}

	GridFSDBFile getGridFSDBFileByID(ObjectId objectId) {
		this.getGridFS().findOne(objectId)
	}

	List getFilesUploadedByUser(user) {
		UploadedFile.findAllByUploader(user)
	}

	List getUploadedFilesFromAssaysReadableByUser(user) {
		UploadedFile.findAllByAssayInList(assayService.getAssaysReadableByUser(user))
	}

	List getUploadedFilesFromAssaysWritableByUser(user) {
		UploadedFile.findAllByAssayInList(assayService.getAssaysWritableByUser(user))
	}

	List getUploadedFilesForUser(user) {
		(getFilesUploadedByUser(user) + getUploadedFilesFromAssaysWritableByUser(user)).unique()
	}

	def deleteUploadedFile(UploadedFile uploadedFile) {
		this.getGridFS().remove(new ObjectId(uploadedFile.gridFSFile_id))
		uploadedFile.delete()
	}

	/**
	 * Parses contents of an GridFS file.
	 * The method checks whether all rows are equally long and whether the dimensions exceed 2x2.
	 *
	 * @param uploadedFile
	 * @param hints
	 * @return
	 */
	UploadedFile parseUploadedFile(UploadedFile uploadedFile, Map hints = [:]) {
		def inputStream = uploadedFile.file.inputStream
		def (matrix, parseInfo) = MatrixImporter.instance.importInputStream(inputStream, hints + [fileName: uploadedFile.fileName], true)

		if (!matrix) {
			throw new RuntimeException("Error parsing file ${uploadedFile.fileName}; resulting data matrix is empty.")
		}

		// check whether all rows have equal length
		if ((matrix*.size().unique()).size > 1) {
			throw new RuntimeException("Error parsing file ${uploadedFile.fileName}; every row should have the same number of columns.")
		}

		uploadedFile.matrix = matrix
		uploadedFile.rows = matrix.size
		uploadedFile.columns = matrix[0].size
		uploadedFile.parseInfo = parseInfo

		// check whether dimensions are at least 2x2
		if (uploadedFile.rows < 2 || uploadedFile.columns < 2) {
			throw new RuntimeException("Error importing file: file should have at least two rows and two columns. Rows: $uploadedFile.rows columns: $uploadedFile.columns.")
		}

		uploadedFile
	}

	/**
	 * Gets data from columns with specified columnIndices. Does not include header in result.
	 *
	 * @param uploadedFile
	 * @param columnIndices
	 * @return columns * rows structured array
	 */
	ArrayList getDataFromColumns(UploadedFile uploadedFile, ArrayList columnIndices) {
		// TODO: check whether this can be more efficient
		def transposedData = uploadedFile?.matrix?.transpose()

		if (!transposedData) return []

		transposedData[columnIndices].collect { it[(uploadedFile.featureRowIndex + 1)..-1] }
	}

	/**
	 * Gets data from the column with specified columnIndex. Does not include header in result.
	 *
	 * @param uploadedFile
	 * @param columnIndex
	 * @return
	 */
	ArrayList getDataFromColumn(UploadedFile uploadedFile, Integer columnIndex) {
		getDataFromColumns(uploadedFile, [columnIndex])[0] as ArrayList
	}

	/**
	 * Returns the contents of the header row of a parsed file.
	 *
	 * @param uploadedFile
	 * @return
	 */
	ArrayList getHeaderRow(UploadedFile uploadedFile) {
		uploadedFile.matrix[uploadedFile.featureRowIndex]
	}

	/**
	 * Returns indices from columns that contain data. The sample column and indices from uploadedFile.ignoredDataColumns
	 * are omitted.
	 *
	 * @param uploadedFile
	 * @return
	 */
	ArrayList getDataColumnIndices(UploadedFile uploadedFile) {
		(0..uploadedFile.columns - 1) - uploadedFile.sampleColumnIndex - uploadedFile.ignoredDataColumns
	}

	/**
	 * Returns measurement names from the header of the data matrix. The sample column and columns with indices from
	 * uploadedFile.ignoredDataColumns are not returned.
	 *
	 * @param uploadedFile
	 * @return
	 */
	ArrayList getFeatureNames(UploadedFile uploadedFile) {
		getHeaderRow(uploadedFile)[getDataColumnIndices(uploadedFile)]
	}

	/**
	 * Returns data columns for given measurementTokens.
	 *
	 * @param UploadedFile
	 * @param measurementTokens
	 * @return
	 */
	ArrayList getDataForMeasurementTokens(UploadedFile UploadedFile, measurementTokens) {
		ArrayList columnIndices = getHeaderRow(UploadedFile).findIndexValues { it in measurementTokens }
		getDataFromColumns(UploadedFile, columnIndices)
	}

	/**
	 * Returns data from the columns that contain measurement data.
	 *
	 * @param UploadedFile
	 * @return
	 */
	ArrayList getMeasurementData(UploadedFile UploadedFile) {
		ArrayList dataColumns = getDataColumnIndices(UploadedFile)
		getDataFromColumns(UploadedFile, dataColumns)
	}

	/**
	 * Returns sample names from the parsed file.
	 * It does this by return the data for the column marked as 'sampleColumnIndex'
	 *
	 * @param UploadedFile
	 * @return
	 */
	ArrayList getSampleNames(UploadedFile uploadedFile) {
		getDataFromColumn(uploadedFile, uploadedFile?.sampleColumnIndex)
	}

	/**
	 * Retrieves row indices that belong to given sample names.
	 *
	 * @param UploadedFile
	 * @param sampleNames
	 * @return
	 */
	ArrayList getRowIndicesForSampleNames(UploadedFile uploadedFile, sampleNames) {
		uploadedFile.matrix.findIndexValues { row -> row[uploadedFile.sampleColumnIndex] in sampleNames }
	}

	/**
	 * Returns data for specific samples
	 *
	 * @param UploadedFile
	 * @param sampleNames
	 * @return
	 */
	ArrayList getDataForSampleNames(UploadedFile uploadedFile, sampleNames) {
		def rows = getRowIndicesForSampleNames(uploadedFile, sampleNames)
		getMeasurementData(uploadedFile)[rows]
	}

	/**
	 * Returns data for specific samples and measurementTokens
	 *
	 * @param UploadedFile
	 * @param sampleNames
	 * @param measurementTokens
	 * @return
	 */
	ArrayList getDataForSamplesTokensAndMeasurementTokens(UploadedFile uploadedFile, sampleTokens, measurementTokens) {
		def sampleNames = Sample.findAllBySampleTokenInList(sampleTokens)*.name
		def rows = getRowIndicesForSampleNames(uploadedFile, sampleNames)

		getDataForMeasurementTokens(uploadedFile, measurementTokens).transpose()[rows]
	}

	UploadedFile transposeMatrix(UploadedFile uploadedFile) {
		uploadedFile.matrix = uploadedFile.matrix.transpose()
		uploadedFile.rows = uploadedFile.matrix.size
		uploadedFile.columns = uploadedFile.matrix[0].size()
		uploadedFile.isColumnOriented = !uploadedFile.isColumnOriented

		return uploadedFile
	}

	def sampleCount(UploadedFile uploadedFile) {
		uploadedFile.rows - uploadedFile.featureRowIndex - 1
	}
}
