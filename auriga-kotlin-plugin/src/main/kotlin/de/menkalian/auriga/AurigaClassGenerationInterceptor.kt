package de.menkalian.auriga

import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.codegen.ClassBuilderFactory
import org.jetbrains.kotlin.codegen.extensions.ClassBuilderInterceptorExtension
import org.jetbrains.kotlin.diagnostics.DiagnosticSink
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin

class AurigaClassGenerationInterceptor(val aurigaAnnotations: List<String>) : ClassBuilderInterceptorExtension {
    override fun interceptClassBuilderFactory(
        interceptedFactory: ClassBuilderFactory,
        bindingContext: BindingContext,
        diagnostics: DiagnosticSink
                                             ): ClassBuilderFactory =
        object : ClassBuilderFactory by interceptedFactory {
            override fun newClassBuilder(origin: JvmDeclarationOrigin): ClassBuilder {
                return AurigaClassBuilder(
                    aurigaAnnotations,
                    interceptedFactory.newClassBuilder(origin)
                                         )
            }
        }

}