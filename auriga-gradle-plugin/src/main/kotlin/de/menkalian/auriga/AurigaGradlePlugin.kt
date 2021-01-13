@file:Suppress("unused")

package de.menkalian.auriga

import de.menkalian.auriga.config.Auriga
import de.menkalian.auriga.config.AurigaConfig
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.tasks.compile.JavaCompile
import java.net.URI
import java.util.Collections
import java.util.concurrent.atomic.AtomicBoolean

const val EXTENSION_NAME = "auriga"

class AurigaGradlePlugin : org.gradle.api.Plugin<Project> {
    private val baseApplied = AtomicBoolean(false)
    private lateinit var extension: AurigaGradleExtension

    override fun apply(project: Project) {
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

        extension = project.extensions.create(EXTENSION_NAME, AurigaGradleExtension::class.java, project)

        project.pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
            applyBaseIfNotAlreadyApplied(project)
            project.pluginManager.apply(AurigaGradleSubplugin::class.java)
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
            val optionsWithKey = extension.getOptionsWithKey()
            // If any templates are given, we need to use a temporary file, since multiline-parameters can cause issues
            if (extension.type == "ARGS" && optionsWithKey.any { Auriga.Logging.Template.getKeys().contains(it.key) }) {
                val temporaryConfigFile = createTempFile("auriga_cfg", ".xml").absoluteFile
                val config = AurigaConfig(Collections.unmodifiableMap(optionsWithKey))
                config.saveToFile(temporaryConfigFile)

                addJavacOption(project, Auriga.Config.type, "FILE")
                addJavacOption(project, Auriga.Config.location, temporaryConfigFile.absolutePath)
            } else {
                optionsWithKey.forEach { addJavacOption(project, it.key, it.value) }
            }
        }
    }

    private fun applyBaseIfNotAlreadyApplied(project: Project) {
        if (!baseApplied.getAndSet(true)) {
            // Add the Annotations dependency
            project.dependencies.add("implementation", "de.menkalian.auriga:auriga-annotations:1.0.0")
        }
    }

    private fun addJavacOption(project: Project, key: String, value: String) {
        project.logger.info("Auriga: Adding Arguments to JavaCompile Tasks")
        project.logger.debug("Auriga: Adding $key -> $value to JavaCompile Tasks")
        project.tasks.withType(JavaCompile::class.java).all {
            it.options.compilerArgs.add("-A$key=$value")
        }
    }
}

internal fun Project.auriga(): AurigaGradleExtension =
    extensions.getByName(EXTENSION_NAME) as? AurigaGradleExtension
        ?: throw IllegalStateException("Extension '$EXTENSION_NAME' does not have the correct type. Maybe there is an issue with your dependencies?")
