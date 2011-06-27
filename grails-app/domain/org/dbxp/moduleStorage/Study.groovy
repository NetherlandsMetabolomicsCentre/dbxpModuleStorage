package org.dbxp.moduleStorage

class Study {

    static hasMany = [dataMatrices: DataMatrix, assays: Assay]

    static constraints = {
    }
}
