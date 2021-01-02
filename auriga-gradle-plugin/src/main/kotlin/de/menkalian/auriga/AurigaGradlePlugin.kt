package de.menkalian.auriga

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
            addJavacOption(project, "auriga.base", gradleExtension.base.name)
        }
    }

    private fun addJavacOption(project: Project, key: String, value: String) {
        project.tasks.withType(JavaCompile::class.java).all {
            it.options.compilerArgs.add("-A$key=$value")
        }
    }
}


open class AurigaGradleExtension @Inject constructor(val objectFactory: ObjectFactory) {
    var base: AurigaBaseType = AurigaBaseType.PRINT
    var config: NamedDomainObjectContainer<AurigaGradleConfigExtension> = objectFactory.domainObjectContainer(AurigaGradleConfigExtension::class.java)
    var log: NamedDomainObjectContainer<AurigaGradleLogExtension> = objectFactory.domainObjectContainer(AurigaGradleLogExtension::class.java)
}

open class AurigaGradleConfigExtension {
    var type: AurigaConfigType = AurigaConfigType.ARGS
    var location: String = ""
}

open class AurigaGradleLogExtension @Inject constructor(val objectFactory: ObjectFactory){
    var placeholder: AurigaLogPlaceholderType = AurigaLogPlaceholderType.NONE
    var method: String = ""
    var logger = objectFactory.domainObjectContainer(AurigaLoggerType::class.java)
    var callTemplate: String = ""
    var paramTemplate: String = ""
}

open class AurigaLoggerType(var clazz: String, var provisioning: String, var type: String = "CUSTOM") {
    companion object {
        val NONE = AurigaLoggerType("", "", "NONE")
        val SLF4J = AurigaLoggerType("", "", "SLF4J")
    }
}

enum class AurigaLogPlaceholderType {
    PRINTF, SLF4J, NONE
}

enum class AurigaConfigType {
    FILE, ARGS
}

enum class AurigaBaseType {
    PRINT, SLF4J
}