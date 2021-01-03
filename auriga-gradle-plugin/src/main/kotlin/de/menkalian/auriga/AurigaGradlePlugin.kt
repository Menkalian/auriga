package de.menkalian.auriga

import de.menkalian.auriga.config.Auriga
import de.menkalian.auriga.config.AurigaLoggerConfig
import de.menkalian.auriga.config.AurigaLoggingConfig
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.compile.JavaCompile
import java.net.URI
import javax.inject.Inject

class AurigaGradlePlugin : org.gradle.api.Plugin<Project> {
    override fun apply(project: Project) {
        // Create the configuration space
        project.extensions.create(
            "auriga", AurigaGradleExtension::class.java
        )
        // Add the Annotations dependency
        project.dependencies.add("implementation", "de.menkalian.auriga:auriga-annotations:1.0.0")

        // Add the repository if needed
        if (project.repositories.any {
                if (it is MavenArtifactRepository) {
                    it.url.toString() == "http://server.menkalian.de:8081/artifactory/auriga"
                } else false
            }) {
            project.repositories.maven {
                it.name = "artifactory-auriga-menkalian"
                it.url = URI("http://server.menkalian.de:8081/artifactory/auriga")
            }
        }

        // Check if java is present
        if (project.pluginManager.hasPlugin("java")) {
            project.dependencies.add("annotationProcessor", "de.menkalian.auriga:auriga-java-processor:1.0.0")

            val gradleExtension = project.extensions.getByType(AurigaGradleExtension::class.java)

            addJavacOption(project, Auriga.Config.base, gradleExtension.base)
            addJavacOption(project, Auriga.Config.location, gradleExtension.location)
            addJavacOption(project, Auriga.Config.type, gradleExtension.type)

            addJavacOption(project, Auriga.Logging.method, gradleExtension.logging.method)
            addJavacOption(project, Auriga.Logging.mode, gradleExtension.logging.mode)
            addJavacOption(project, Auriga.Logging.placeholder, gradleExtension.logging.placeholder)
            addJavacOption(project, Auriga.Logging.Template.entry, gradleExtension.logging.entryTemplate)
            addJavacOption(project, Auriga.Logging.Template.param, gradleExtension.logging.paramTemplate)
            addJavacOption(project, Auriga.Logger.type, gradleExtension.logger.type)
            addJavacOption(project, Auriga.Logger.clazz, gradleExtension.logger.clazz)
            addJavacOption(project, Auriga.Logger.source, gradleExtension.logger.source)
        }
    }

    private fun addJavacOption(project: Project, key: String, value: String) {
        project.tasks.withType(JavaCompile::class.java).all {
            if(value.isNotBlank()){
                project.logger.lifecycle("Adding $key -> $value")
                it.options.compilerArgs.add("-A$key=$value")
            }
        }
    }
}


open class AurigaGradleExtension @Inject constructor(objectFactory: ObjectFactory) {
    open var base: String = ""
    open var type: String = "ARGS"
    open var location: String = ""

    open var logging: AurigaLoggingConfig = objectFactory.newInstance(AurigaLoggingConfig::class.java)
    open var logger: AurigaLoggerConfig = objectFactory.newInstance(AurigaLoggerConfig::class.java)
}
