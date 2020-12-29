package de.menkalian.auriga

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.codegen.extensions.ClassBuilderInterceptorExtension
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration

@AutoService(ComponentRegistrar::class)
class AurigaComponentRegistrar : ComponentRegistrar {
    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
        ClassBuilderInterceptorExtension.registerExtension(
            project, AurigaClassGenerationInterceptor(
                configuration[AurigaConfigurationKeys.KEY_ANNOTATION] ?: error("Auriga needs!")
            )
        )
    }
}