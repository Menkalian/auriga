package de.menkalian.auriga.config

object Auriga {
    private const val BASE = "auriga"

    fun getKeys(): Set<String> {
        val toReturn = mutableSetOf<String>()
        toReturn.addAll(Config.getKeys())
        toReturn.addAll(Logger.getKeys())
        toReturn.addAll(Logging.getKeys())
        return toReturn
    }

    object Config {
        private const val BASE = "${Auriga.BASE}.config"

        const val type = "$BASE.type"
        const val location = "$BASE.location"
        const val base = "$BASE.base"

        fun getKeys(): Set<String> {
            val toReturn = mutableSetOf<String>()
            toReturn.add(type)
            toReturn.add(location)
            toReturn.add(base)
            return toReturn
        }
    }

    object Logger {
        private const val BASE = "${Auriga.BASE}.logger"

        const val type = "$BASE.type"
        const val clazz = "$BASE.clazz"
        const val source = "$BASE.source"

        fun getKeys(): Set<String> {
            val toReturn = mutableSetOf<String>()
            toReturn.add(type)
            toReturn.add(clazz)
            toReturn.add(source)
            return toReturn
        }
    }

    object Logging {
        private const val BASE = "${Auriga.BASE}.logging"

        const val mode = "$BASE.mode"
        const val method = "$BASE.method"
        const val placeholder = "$BASE.placeholder"

        fun getKeys(): Set<String> {
            val toReturn = mutableSetOf<String>()
            toReturn.add(mode)
            toReturn.add(method)
            toReturn.add(placeholder)
            toReturn.addAll(Template.getKeys())
            return toReturn
        }

        object Template {
            private const val BASE = "${Logging.BASE}.template"

            const val entry = "$BASE.entry"
            const val param = "$BASE.param"

            fun getKeys(): Set<String> {
                val toReturn = mutableSetOf<String>()
                toReturn.add(entry)
                toReturn.add(param)
                return toReturn
            }
        }
    }
}