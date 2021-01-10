@file:Suppress("unused")

package de.menkalian.auriga

import de.menkalian.auriga.config.Auriga
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.tasks.compile.JavaCompile
import java.net.URI
import java.util.concurrent.atomic.AtomicBoolean

const val EXTENSION_NAME = "auriga"

class AurigaGradlePlugin : org.gradle.api.Plugin<Project> {
    private val baseApplied = AtomicBoolean(false)
    private lateinit var extension: AurigaGradleExtension

    override fun apply(project: Project) {
        extension = project.extensions.create(EXTENSION_NAME, AurigaGradleExtension::class.java, project)
        project.afterEvaluate {
            println(extension)
        }

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

        project.afterEvaluate {
            addJavacOption(project, Auriga.Config.base, extension.base)
            addJavacOption(project, Auriga.Config.location, extension.location)
            addJavacOption(project, Auriga.Config.type, extension.type)

            addJavacOption(project, Auriga.Logging.method, extension.loggingConfig.method)
            addJavacOption(project, Auriga.Logging.mode, extension.loggingConfig.mode)
            addJavacOption(project, Auriga.Logging.placeholder, extension.loggingConfig.placeholder)
            addJavacOption(project, Auriga.Logging.Template.entry, extension.loggingConfig.entryTemplate)
            addJavacOption(project, Auriga.Logging.Template.param, extension.loggingConfig.paramTemplate)
            addJavacOption(project, Auriga.Logger.type, extension.loggerConfig.type)
            addJavacOption(project, Auriga.Logger.clazz, extension.loggerConfig.clazz)
            addJavacOption(project, Auriga.Logger.source, extension.loggerConfig.source)
        }
    }

    private fun applyBaseIfNotAlreadyApplied(project: Project) {
        if (!baseApplied.getAndSet(true)) {
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

internal fun Project.auriga() : AurigaGradleExtension =
    extensions.getByName(EXTENSION_NAME) as? AurigaGradleExtension ?:
    throw IllegalStateException("Extension '$EXTENSION_NAME' does not have the correct type. Maybe there is an issue with your dependencies?")
