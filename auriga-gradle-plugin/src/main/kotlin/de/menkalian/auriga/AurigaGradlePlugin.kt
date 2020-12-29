package de.menkalian.auriga

import org.gradle.api.Project

class AurigaGradlePlugin : org.gradle.api.Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create(
            "auriga",
            AurigaGradleExtension::class.java
                                 )
    }
}

open class AurigaGradleExtension {
    var enabled: Boolean = true
    var annotations: List<String> = emptyList()
}