package de.menkalian.auriga

import org.jetbrains.kotlin.config.CompilerConfigurationKey

object AurigaConfigurationKeys {
    val KEY_CONFIG_TYPE = CompilerConfigurationKey.create<String>("type of configuration")
    val KEY_CONFIG_LOCATION = CompilerConfigurationKey.create<String>("location of configuration")
    val KEY_CONFIG_BASE = CompilerConfigurationKey.create<String>("base configuration")

    val KEY_LOGGER_TYPE = CompilerConfigurationKey.create<String>("type of logger")
    val KEY_LOGGER_CLAZZ = CompilerConfigurationKey.create<String>("class of logger")
    val KEY_LOGGER_SOURCE = CompilerConfigurationKey.create<String>("source of logger")

    val KEY_LOGGING_MODE = CompilerConfigurationKey.create<String>("mode of logging")
    val KEY_LOGGING_METHOD = CompilerConfigurationKey.create<String>("method for logging")
    val KEY_LOGGING_PLACEHOLDER = CompilerConfigurationKey.create<String>("placeholder for logging calls")

    val KEY_LOGGING_TEMPLATE_ENTRY = CompilerConfigurationKey.create<String>("template for logging method entries")
    val KEY_LOGGING_TEMPLATE_PARAM = CompilerConfigurationKey.create<String>("template for logging method parameters")
}