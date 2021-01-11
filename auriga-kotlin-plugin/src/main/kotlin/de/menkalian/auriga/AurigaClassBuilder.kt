package de.menkalian.auriga

import de.menkalian.auriga.config.AurigaConfig
import de.menkalian.auriga.config.Placeholder
import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.codegen.DelegatingClassBuilder
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.resolve.descriptorUtil.parents
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter

class AurigaClassBuilder(val config: AurigaConfig, private val delegateBuilder: ClassBuilder) : DelegatingClassBuilder() {
    override fun getDelegate(): ClassBuilder = delegateBuilder

    override fun newMethod(origin: JvmDeclarationOrigin, access: Int, name: String, desc: String, signature: String?, exceptions: Array<out String>?
    ): MethodVisitor {
        val original = super.newMethod(origin, access, name, desc, signature, exceptions)
        val function = origin.descriptor as? FunctionDescriptor ?: return original

//        val modifyMethod = when (config.loggingConfig.mode) {
//            "DEFAULT_OFF" -> function.parentsWithSelf.any { it.annotations.hasAnnotation(FqName("de.menkalian.auriga.Log")) }
//            "DEFAULT_ON"  -> function.parentsWithSelf.none { it.annotations.hasAnnotation(FqName("de.menkalian.auriga.NoLog")) }
//            else          -> error("Unknown Logging Mode")
//        }

//        if (modifyMethod) return original
        return object : MethodVisitor(Opcodes.ASM5, original) {
            override fun visitCode() {
                super.visitCode()
                InstructionAdapter(this).apply {
                    getstatic("java/lang/System", "out", "Ljava/io/PrintStream;")
                    anew(Type.getType(StringBuilder::class.java))
                    dup()
                    invokespecial("java/lang/StringBuilder", "<init>", "()V", false)

                    val template = config.loggingConfig.entryTemplate
                        .replace(Placeholder.METHOD, function.name.toString())
                        .replace(Placeholder.CLASS, function.parents.first().name.toString())

                    val templateSplit = template.split("{{")
                    templateSplit.forEach {
                        it.split("}}").forEach {
                            when (it) {
                                "THIS"   -> {
                                    visitVarInsn(Opcodes.ALOAD, -1)
                                    invokevirtual("java/lang/Object", "toString", "()Ljava/lang/String;", false)
                                    invokevirtual("java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;", false)
                                }
                                "PARAMS" -> {
                                    function.valueParameters.forEachIndexed { i, parameterDescriptor ->
                                        val paramTemplate = config.loggingConfig.paramTemplate
                                            .replace(Placeholder.PARAM_NAME, parameterDescriptor.name.identifier)
                                            .replace(Placeholder.PARAM_TYPE, parameterDescriptor.type.toString())
                                        val paramTemplateSplit = paramTemplate.split(Regex.fromLiteral("(\\{\\{)|(\\}\\})"))
                                        paramTemplateSplit.forEach {
                                            when (it) {
                                                "PARAM_VALUE" -> {
                                                    visitVarInsn(Opcodes.ALOAD, i + 1)
                                                    invokevirtual("java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;", false)
                                                }
                                                else          -> {
                                                    visitLdcInsn(it)
                                                    invokevirtual("java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;", false)
                                                }
                                            }
                                        }
                                    }
                                }
                                else     -> {
                                    visitLdcInsn(it)
                                    invokevirtual("java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;", false)
                                }
                            }
                        }
                    }
                    invokevirtual("java/lang/Object", "toString", "()Ljava/lang/String;", false)
                    invokevirtual("java/io/PrintStream", "println", "(Ljava/lang/String;)V", false)
                }
            }
        }
    }
}