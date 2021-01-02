package de.menkalian.auriga.config

import java.lang.reflect.Field

enum class FormatPlaceholder(val placeholder: String) {
    PRINTF("%s"), SLF4J("{}"), NONE("%s");
}

object Placeholder {
    const val CLASS = "{{CLASS}}"
    const val METHOD = "{{METHOD}}"
    const val THIS = "{{THIS}}"
    const val PARAMS = "{{PARAMS}}"
    const val PARAM_TYPE = "{{PARAM_TYPE}}"
    const val PARAM_NAME = "{{PARAM_NAME}}"
    const val PARAM_VALUE = "{{PARAM_VALUE}}"
}

fun getSupportedConfigKeys(): Set<String> {
    return Auriga.getKeys()
}