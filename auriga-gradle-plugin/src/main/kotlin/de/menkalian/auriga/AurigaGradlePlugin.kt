package de.menkalian.auriga

import org.gradle.api.Project

class AurigaGradlePlugin : org.gradle.api.Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create(
            "auriga",
            AurigaGradleExtension::class.java
                                 )

        // TODO: 29.12.2020
        project.dependencies.add("implementation", "de.menkalian.auriga:annotations:1.0.0")
    }
}

open class AurigaGradleExtension {
    var enabled: Boolean = true
    var annotations: List<String> = emptyList()
}