@file:Suppress("MemberVisibilityCanBePrivate")

package de.menkalian.auriga.config

import java.io.File
import java.util.Properties

open class AurigaConfig(val type: String = "FILE", val location: String = "auriga-config.xml", map: Map<Any, Any> = emptyMap()) {

    constructor(map: Map<Any, Any>) : this(
        map.getOrDefault(Auriga.Config.type, "FILE").toString(),
        map.getOrDefault(Auriga.Config.location, "auriga-config.xml").toString(),
        map
    )

    var base = "PRINT"
    val loggingConfig = AurigaLoggingConfig()
    val loggerConfig = AurigaLoggerConfig()

    init {
        if (type == "FILE") {
            val f = File(location)
            val props = Properties()

            if (location.endsWith(".xml")) {
                props.loadFromXML(f.inputStream())
            } else {
                props.load(f.inputStream())
            }
            loadConfigFromMap(props)
        } else if(type=="ARGS"){
            loadConfigFromMap(map)
        }
    }

    private fun loadConfigFromMap(map: Map<Any, Any>) {
        base = (map[Auriga.Config.base] ?: base).toString()
        loadBaseConfig()

        loggingConfig.mode = (map[Auriga.Logging.mode] ?: loggingConfig.mode).toString()
        loggingConfig.method = (map[Auriga.Logging.method] ?: loggingConfig.method).toString()
        loggingConfig.placeholder = (map[Auriga.Logging.placeholder] ?: loggingConfig.placeholder).toString()
        loggingConfig.entryTemplate = (map[Auriga.Logging.Template.entry] ?: loggingConfig.entryTemplate).toString()
        loggingConfig.paramTemplate = (map[Auriga.Logging.Template.param] ?: loggingConfig.paramTemplate).toString()

        loggerConfig.type = (map[Auriga.Logger.type] ?: loggerConfig.type).toString()
        loggerConfig.clazz = (map[Auriga.Logger.clazz] ?: loggerConfig.clazz).toString()
        loggerConfig.source = (map[Auriga.Logger.source] ?: loggerConfig.source).toString()
    }

    private fun loadBaseConfig() {
        when (base) {
            "PRINT" -> {/* loaded by default */
            }
            "SLF4J" -> {
                loggingConfig.method = "log.debug"
                loggingConfig.placeholder = "SLF4J"
                loggerConfig.type = "SLF4J"
            }
            else    -> error("Unknown base config")
        }
    }
}

open class AurigaLoggingConfig {
    var mode : String = "DEFAULT_OFF"
    var method: String = "System.out.printf"
    var placeholder = "PRINTF"
    var entryTemplate = "Executing {{METHOD}} with Params: {\n{{PARAMS}}\n}\n"
    var paramTemplate = "    {{PARAM_NAME}} : {{PARAM_TYPE}} = {{PARAM_VALUE}}"

    fun getPlaceholderEnum() : FormatPlaceholder {
        return FormatPlaceholder.valueOf(placeholder)
    }
}

open class AurigaLoggerConfig {
    var type = "NONE"
    var clazz = "org.slf4j.Logger"
    var source = "org.slf4j.LoggerFactory.getLogger({{CLASS}}.class)"
}