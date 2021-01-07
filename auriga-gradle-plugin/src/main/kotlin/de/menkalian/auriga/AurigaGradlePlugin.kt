@file:Suppress("unused")

package de.menkalian.auriga

import de.menkalian.auriga.config.Auriga
import de.menkalian.auriga.config.AurigaLoggerConfig
import de.menkalian.auriga.config.AurigaLoggingConfig
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.compile.JavaCompile
import java.net.URI
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

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

        val gradleExtension: AurigaGradleExtension
        try {
            gradleExtension = project.extensions.getByName("auriga") as AurigaGradleExtension
            println(gradleExtension)
        } catch (ex: Exception) {
            ex.printStackTrace()
            return
        }

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
        if (!baseApplied.getAndSet(true)) {
            // Create the configuration space
            try {

                project.extensions.create(
                    "auriga", AurigaGradleExtension::class.java, project
                )
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

            // https://github.com/andriipanasiuk/family-gradle-plugin/blob/master/plugin/src/main/groovy/com/andriipanasiuk/family/plugin/FamilyPlugin.groovy
            // https://medium.com/friday-insurance/how-to-write-a-gradle-plugin-in-kotlin-68d7a3534e71
            // https://github.com/rpsrosario/elasticmq-gradle-plugin/blob/master/src/main/kotlin/ServerInstanceConfiguration.kt
            // https://stackoverflow.com/questions/64238451/execution-failed-for-task-void-kotlin-jvm-internal-mutablepropertyreference1i
            // https://github.com/tonsV2/gradle-helm-release/blob/feature/post_chart_using_fuel/build.gradle


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

open class AurigaGradleExtension @Inject constructor(val project: Project, val objectFactory: ObjectFactory) {
    var base : String by GradleProperty(project, String::class.java, "")
    open var type: String = "ARGS"
    open var location: String = ""

    open var loggingConfig: AurigaLoggingConfig = AurigaLoggingConfig()
    open var loggerConfig: AurigaLoggerConfig = AurigaLoggerConfig()

    init {
        println("Initializing Extension")
    }

    override fun toString(): String {
        return "AurigaGradleExtension(project=$project, base='${base}', type='$type', location='$location', loggingConfig=$loggingConfig, loggerConfig=$loggerConfig)"
    }
}
