package de.menkalian.auriga

import org.jetbrains.kotlin.config.CompilerConfigurationKey

object AurigaConfigurationKeys {
    val KEY_ENABLED = CompilerConfigurationKey.create<Boolean>("enabled state")
    val KEY_ANNOTATION = CompilerConfigurationKey.create<List<String>>("annotation qualified name")
}