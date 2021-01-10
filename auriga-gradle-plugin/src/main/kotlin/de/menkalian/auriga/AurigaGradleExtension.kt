package de.menkalian.auriga

import de.menkalian.auriga.config.AurigaLoggerConfig
import de.menkalian.auriga.config.AurigaLoggingConfig
import groovy.lang.Closure
import org.gradle.api.Project

open class AurigaGradleExtension constructor(val project: Project) {
    var base: String = ""
    var type: String = "ARGS"
    var location: String = ""

    var loggingConfig: AurigaLogging = AurigaLogging()
    var loggerConfig: AurigaLogger = AurigaLogger()

    open fun loggingConfig(closure: Closure<*>): AurigaLogging =
        project.configure(loggingConfig, closure) as AurigaLogging

    open fun loggingConfig(config: AurigaLogging.() -> Unit) =
        loggingConfig.config()

    open fun loggerConfig(closure: Closure<*>): AurigaLogger =
        project.configure(loggerConfig, closure) as AurigaLogger

    open fun loggerConfig(config: AurigaLogger.() -> Unit) =
        loggerConfig.config()
}

open class AurigaLogging : AurigaLoggingConfig() {
    init {
        method = ""
        mode = ""
        placeholder = ""
        entryTemplate = ""
        paramTemplate = ""
    }
}

open class AurigaLogger : AurigaLoggerConfig() {
    init {
        type = ""
        clazz = ""
        source = ""
    }
}