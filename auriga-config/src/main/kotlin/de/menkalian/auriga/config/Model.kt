package de.menkalian.auriga.config

import java.io.File
import java.util.Properties

class AurigaConfig(val type: String = "FILE", val location: String = "auriga-config.xml") {

    constructor(properties: Properties) : this(
        properties.getProperty(Auriga.Config.type, "FILE"),
        properties.getProperty(Auriga.Config.location, "auriga-config.xml")
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
        }
    }

    fun loadConfigFromMap(map: Map<Any, Any>) {
        base = (map[Auriga.Config.base] ?: base).toString()
        loadBaseConfig()

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
                loggingConfig.placeholder = "log.debug"
                loggingConfig.placeholder = "SLF4J"
                loggerConfig.type = "SLF4J"
            }
            else    -> error("Unknown base config")
        }
    }
}

class AurigaLoggingConfig {
    var method: String = "System.out.printf"
    var placeholder = "PRINTF"
    var entryTemplate = "Executing {{METHOD}} with Params: {\n{{PARAMS}}\n}\n"
    var paramTemplate = "    {{PARAM_NAME}} : {{PARAM_TYPE}} = {{PARAM_VALUE}}"

    fun getPlaceholderEnum() : FormatPlaceholder {
        return FormatPlaceholder.valueOf(placeholder)
    }
}

class AurigaLoggerConfig {
    var type = "NONE"
    var clazz = "org.slf4j.Logger"
    var source = "org.slf4j.LoggerFactory.getLogger({{CLASS}}.class)"
}