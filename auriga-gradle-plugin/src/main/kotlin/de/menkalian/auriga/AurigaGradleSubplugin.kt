package de.menkalian.auriga

import com.google.auto.service.AutoService
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

@AutoService(KotlinCompilerPluginSupportPlugin::class)
class AurigaGradleSubplugin : KotlinCompilerPluginSupportPlugin {
    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        return kotlinCompilation.target.project.provider {
            val extension = kotlinCompilation.target.project.extensions.findByType(AurigaGradleExtension::class.java)

            println(extension)
            listOf(SubpluginOption("tmp", "tmp"))
        }
    }

    override fun getCompilerPluginId(): String = "auriga"


    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = "de.menkalian", artifactId = "auriga-kotlin-plugin", version = "1.0.0"
    )

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean =
        kotlinCompilation.target.project.plugins.hasPlugin(AurigaGradlePlugin::class.java)
}