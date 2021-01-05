@file:Suppress("unused")

package de.menkalian.auriga

import de.menkalian.auriga.config.Auriga
import de.menkalian.auriga.config.AurigaLoggerConfig
import de.menkalian.auriga.config.AurigaLoggingConfig
import groovy.lang.Closure
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.tasks.compile.JavaCompile
import java.net.URI
import java.util.concurrent.atomic.AtomicBoolean

class AurigaGradlePlugin : org.gradle.api.Plugin<Project> {
    private val baseApplied = AtomicBoolean(false)

    override fun apply(project: Project) {
        project.pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
            applyBaseIfNotAlreadyApplied(project)
        }

        // Check if java is present
        project.pluginManager.withPlugin("java") {
            applyJavaParams(project)
        }

        // Android has java as well
        project.pluginManager.withPlugin("com.android.application") {
            applyJavaParams(project)
        }
        project.pluginManager.withPlugin("com.android.library") {
            applyJavaParams(project)
        }
    }

    private fun applyJavaParams(project: Project) {
        applyBaseIfNotAlreadyApplied(project)

        project.dependencies.add("annotationProcessor", "de.menkalian.auriga:auriga-java-processor:1.0.0")

        val gradleExtension = project.extensions.getByType(AurigaGradleExtension::class.java)
        println(gradleExtension)

        addJavacOption(project, Auriga.Config.base, gradleExtension.base)
        addJavacOption(project, Auriga.Config.location, gradleExtension.location)
        addJavacOption(project, Auriga.Config.type, gradleExtension.type)

        addJavacOption(project, Auriga.Logging.method, gradleExtension.loggingConfig.method)
        addJavacOption(project, Auriga.Logging.mode, gradleExtension.loggingConfig.mode)
        addJavacOption(project, Auriga.Logging.placeholder, gradleExtension.loggingConfig.placeholder)
        addJavacOption(project, Auriga.Logging.Template.entry, gradleExtension.loggingConfig.entryTemplate)
        addJavacOption(project, Auriga.Logging.Template.param, gradleExtension.loggingConfig.paramTemplate)
        addJavacOption(project, Auriga.Logger.type, gradleExtension.loggerConfig.type)
        addJavacOption(project, Auriga.Logger.clazz, gradleExtension.loggerConfig.clazz)
        addJavacOption(project, Auriga.Logger.source, gradleExtension.loggerConfig.source)
    }

    private fun applyBaseIfNotAlreadyApplied(project: Project) {
        if(!baseApplied.getAndSet(true)) {
            // Create the configuration space
            project.extensions.create(
                "auriga", AurigaGradleExtension::class.java, project
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
        }
    }

    private fun addJavacOption(project: Project, key: String, value: String) {
        project.tasks.withType(JavaCompile::class.java).all {
            if (value.isNotBlank()) {
                project.logger.lifecycle("Adding $key -> $value")
                it.options.compilerArgs.add("-A$key=$value")
            }
        }
    }
}

open class AurigaGradleExtension constructor(val project: Project) {
    open var base: String = ""
    open var type: String = "ARGS"
    open var location: String = ""

    open var loggingConfig: AurigaLoggingConfig = AurigaLoggingConfig()
    open var loggerConfig: AurigaLoggerConfig = AurigaLoggerConfig()

    open fun loggingConfig(closure: Closure<AurigaLoggingConfig>): AurigaLoggingConfig {
        project.configure(loggingConfig, closure)
        return loggingConfig
    }

    open fun loggerConfig(closure: Closure<AurigaLoggerConfig>): AurigaLoggerConfig {
        project.configure(loggerConfig, closure)
        return loggerConfig
    }

    override fun toString(): String {
        return "AurigaGradleExtension(project=$project, base='$base', type='$type', location='$location', loggingConfig=$loggingConfig, loggerConfig=$loggerConfig)"
    }

}
