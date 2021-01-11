package de.menkalian.auriga

import com.google.auto.service.AutoService
import de.menkalian.auriga.config.Auriga
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration

@AutoService(CommandLineProcessor::class)
class AurigaCommandLineProcessor : CommandLineProcessor {
    override val pluginId: String = "auriga"
    override val pluginOptions: Collection<AbstractCliOption> = listOf(
        CliOption(Auriga.Config.type, "<ARGS|FILE>", "where to read the configuration from", true),
        CliOption(Auriga.Config.location, "<file>", "config file location (only viable for auriga.config.type FILE)", false),
        CliOption(Auriga.Config.base, "<PRINT|SLF4J>", "base configuration you are deriving from", false),

        CliOption(
            Auriga.Logger.type,
            "<NONE|...>",
            "type of Logger to generate for classes (may be used to set defaults for auriga.logger.clazz and auriga.logger.type)",
            false
        ),
        CliOption(Auriga.Logger.clazz, "<fqname>", "fully qualified class of the logger to generate for classes", false),
        CliOption(Auriga.Logger.source, "<methodcall>", "fully qualified call to get the logger for a class", false),

        CliOption(Auriga.Logging.mode, "<DEFAULT_ON|DEFAULT_OFF>", "mode to determine the default action for non-annotated elements", false),
        CliOption(Auriga.Logging.method, "<fqmethodname>", "Fully qualified name of the method to use for logging", false),
        CliOption(Auriga.Logging.placeholder, "<PRINTF|SLF4J|NONE>", "What flavour of placeholder the logging method uses", false),
        CliOption(Auriga.Logging.Template.entry, "<string>", "template for logging a method", false),
        CliOption(Auriga.Logging.Template.param, "<string>", "template for logging an parameter", false),
    )

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        when (option.optionName) {
            Auriga.Config.type            -> configuration.put(AurigaConfigurationKeys.KEY_CONFIG_TYPE, value)
            Auriga.Config.location        -> configuration.put(AurigaConfigurationKeys.KEY_CONFIG_LOCATION, value)
            Auriga.Config.base            -> configuration.put(AurigaConfigurationKeys.KEY_CONFIG_BASE, value)

            Auriga.Logger.type            -> configuration.put(AurigaConfigurationKeys.KEY_LOGGER_TYPE, value)
            Auriga.Logger.clazz           -> configuration.put(AurigaConfigurationKeys.KEY_LOGGER_CLAZZ, value)
            Auriga.Logger.source          -> configuration.put(AurigaConfigurationKeys.KEY_LOGGER_SOURCE, value)

            Auriga.Logging.mode           -> configuration.put(AurigaConfigurationKeys.KEY_LOGGING_MODE, value)
            Auriga.Logging.method         -> configuration.put(AurigaConfigurationKeys.KEY_LOGGING_METHOD, value)
            Auriga.Logging.placeholder    -> configuration.put(AurigaConfigurationKeys.KEY_LOGGING_PLACEHOLDER, value)
            Auriga.Logging.Template.entry -> configuration.put(AurigaConfigurationKeys.KEY_LOGGING_TEMPLATE_ENTRY, value)
            Auriga.Logging.Template.param -> configuration.put(AurigaConfigurationKeys.KEY_LOGGING_TEMPLATE_PARAM, value)
            else                          -> error("Unexpected config option ${option.optionName}")
        }
    }
}