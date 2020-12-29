package de.menkalian.auriga

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration

@AutoService(CommandLineProcessor::class)
class AurigaCommandLineProcessor : CommandLineProcessor {
    override val pluginId: String = "auriga"
    override val pluginOptions: Collection<AbstractCliOption> = listOf(
        CliOption("enabled", "<true|false>", "whether plugin is enabled"),
        CliOption(
            "aurigaAnnotation", "<fqname>", "auriga annotation names",
            required = true, allowMultipleOccurrences = true
                 )
                                                                      )

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        when (option.optionName) {
            "enabled"          -> configuration.put(AurigaConfigurationKeys.KEY_ENABLED, value.toBoolean())
            "aurigaAnnotation" -> configuration.appendList(AurigaConfigurationKeys.KEY_ANNOTATION, value)
            else               -> error("Unexpected config option ${option.optionName}")
        }
    }
}