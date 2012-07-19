grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
//grails.project.war.file = "target/${appName}-${appVersion}.war"
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        grailsPlugins()
        grailsHome()
        grailsCentral()

        mavenRepo "http://nexus.nmcdsp.org/content/repositories/releases"

        // uncomment the below to enable remote dependency resolution
        // from public Maven repositories
        //mavenLocal()
        //mavenCentral()
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        // runtime 'mysql:mysql-connector-java:5.1.13'
    }
	plugins {


		compile ":hibernate:$grailsVersion"
		compile ":tomcat:$grailsVersion"

		compile ':jquery:1.7.1'
		compile ':jquery-datatables:1.7.5'
		compile ':jquery-ui:1.8.15'
		compile ':famfamfam:1.0.1'
		compile ':resources:1.1.1'

		compile ':mongodb:1.0.0.RC3'

		compile ':matrix-importer:0.2.3.5'
		compile ':dbxp-module-base:0.4.20'
	}
}
//grails.plugin.location.'matrixImporter' = '../matrixImporter'
//grails.plugin.location.'dbxpModuleBase' = '../dbxpModuleBase'