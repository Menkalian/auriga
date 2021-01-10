package de.menkalian.auriga

import de.menkalian.auriga.config.Auriga
import de.menkalian.auriga.config.AurigaLoggerConfig
import de.menkalian.auriga.config.AurigaLoggingConfig
import groovy.lang.Closure
import org.gradle.api.Project

@Suppress("MemberVisibilityCanBePrivate") // Gradle extension should be public
open class AurigaGradleExtension constructor(private val project: Project) {
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

    internal fun getOptionsWithKey() =
        mutableMapOf(
            Pair(Auriga.Config.base, base),
            Pair(Auriga.Config.location, location),
            Pair(Auriga.Config.type, type),

            Pair(Auriga.Logging.method, loggingConfig.method),
            Pair(Auriga.Logging.mode, loggingConfig.mode),
            Pair(Auriga.Logging.placeholder, loggingConfig.placeholder),
            Pair(Auriga.Logging.Template.entry, loggingConfig.entryTemplate),
            Pair(Auriga.Logging.Template.param, loggingConfig.paramTemplate),

            Pair(Auriga.Logger.type, loggerConfig.type),
            Pair(Auriga.Logger.clazz, loggerConfig.clazz),
            Pair(Auriga.Logger.source, loggerConfig.source)
        ).filter { it.value.isNotBlank() }
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