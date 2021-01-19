package de.menkalian.auriga

import de.menkalian.auriga.config.Auriga
import de.menkalian.auriga.config.AurigaConfig
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import java.io.File
import java.util.Collections

class AurigaGradleSubplugin : KotlinCompilerPluginSupportPlugin {

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        return kotlinCompilation.target.project.provider {
            val extension = kotlinCompilation.target.project.auriga()
            val optionsWithKey = extension.getOptionsWithKey()
            val optionsList =
                if (extension.type == "ARGS" && optionsWithKey.any { Auriga.Logging.Template.getKeys().contains(it.key) }) {
                    val temporaryConfigFile = File.createTempFile("auriga_cfg", ".xml")
                    val config = AurigaConfig(Collections.unmodifiableMap(optionsWithKey))
                    config.saveToFile(temporaryConfigFile)

                    listOf(
                        SubpluginOption(Auriga.Config.type, "FILE"),
                        SubpluginOption(Auriga.Config.location, temporaryConfigFile.absolutePath)
                    )
                } else {
                    optionsWithKey.map { SubpluginOption(it.key, it.value) }
                }

            val logger = kotlinCompilation.target.project.logger
            logger.info("Applying auriga kotlin plugin")
            logger.debug("auriga-kotlin-plugin options:")
            optionsList.forEach { logger.debug("${it.key} => ${it.value}") }
            optionsList
        }
    }

    override fun getCompilerPluginId(): String = "auriga"


    override fun getPluginArtifact(): SubpluginArtifact {
        return SubpluginArtifact(
            groupId = "de.menkalian.auriga", artifactId = "auriga-kotlin-plugin", version = "1.0.2"
        )
    }


    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean =
        kotlinCompilation.target.project.plugins.hasPlugin(AurigaGradlePlugin::class.java)
}