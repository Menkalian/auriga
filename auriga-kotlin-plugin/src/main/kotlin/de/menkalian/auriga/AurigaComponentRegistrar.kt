package de.menkalian.auriga

import com.google.auto.service.AutoService
import de.menkalian.auriga.config.Auriga
import de.menkalian.auriga.config.AurigaConfig
import org.jetbrains.kotlin.codegen.extensions.ClassBuilderInterceptorExtension
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

@AutoService(ComponentRegistrar::class)
class AurigaComponentRegistrar : ComponentRegistrar {
    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
        val mapFromCompilerConfig = getMapFromCompilerConfig(configuration)
        val config = AurigaConfig(mapFromCompilerConfig)
        org.jetbrains.kotlin.konan.file.createTempFile("AURIGA").writeText(config.toString())
        ClassBuilderInterceptorExtension.registerExtension(
            project, AurigaClassGenerationInterceptor(
                listOf()
            )
        )
    }

    @Suppress("NO_REFLECTION_IN_CLASS_PATH") // Probably bug (reflect is in classpath)
    private fun getMapFromCompilerConfig(configuration: CompilerConfiguration): Map<Any, Any> {
        val toReturn = mutableMapOf<Any, Any>()

        Auriga.getKeys().forEach { key ->
            val configKey = AurigaConfigurationKeys::class.members.first {
                it.name == "KEY_" + key.substring("auriga.".length).toUpperCase().replace('.', '_')
            }.call(AurigaConfigurationKeys) as? CompilerConfigurationKey<*> ?: return@forEach

            val value = configuration.get(configKey)
            if (value != null)
                toReturn[key] = value
        }

        return toReturn
    }
}