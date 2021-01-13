package de.menkalian.auriga

import de.menkalian.auriga.config.AurigaConfig
import de.menkalian.auriga.config.Placeholder
import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.codegen.DelegatingClassBuilder
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.descriptorUtil.parents
import org.jetbrains.kotlin.resolve.descriptorUtil.parentsWithSelf
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
import org.jetbrains.kotlin.types.typeUtil.isBooleanOrNullableBoolean
import org.jetbrains.kotlin.types.typeUtil.isByte
import org.jetbrains.kotlin.types.typeUtil.isChar
import org.jetbrains.kotlin.types.typeUtil.isDouble
import org.jetbrains.kotlin.types.typeUtil.isFloat
import org.jetbrains.kotlin.types.typeUtil.isInt
import org.jetbrains.kotlin.types.typeUtil.isLong
import org.jetbrains.kotlin.types.typeUtil.isShort
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

        val modifyMethod = when (config.loggingConfig.mode) {
            "DEFAULT_OFF" -> checkFunctionWithDefault(function, false)
            "DEFAULT_ON"  -> checkFunctionWithDefault(function, true)
            else          -> error("Unknown Logging Mode")
        }

        if (!modifyMethod) return original
        return object : MethodVisitor(Opcodes.ASM5, original) {
            override fun visitCode() {
                super.visitCode()
                InstructionAdapter(this).apply {
                    if (config.base == "SLF4J") {
                        visitLdcInsn("Auriga")
                        invokestatic("org/slf4j/LoggerFactory", "getLogger", "(Ljava/lang/String;)Lorg/slf4j/Logger;", false)
                    } else {
                        getstatic("java/lang/System", "out", "Ljava/io/PrintStream;")
                    }
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
                                    visitVarInsn(Opcodes.ALOAD, 0) // aload_0 is this
                                    invokevirtual("java/lang/Object", "toString", "()Ljava/lang/String;", false)
                                    invokevirtual("java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;", false)
                                }
                                "PARAMS" -> {
                                    var index = 0
                                    function.valueParameters.forEach {parameterDescriptor ->
                                        val paramTemplate = config.loggingConfig.paramTemplate
                                            .replace(Placeholder.PARAM_NAME, parameterDescriptor.name.identifier)
                                            .replace(Placeholder.PARAM_TYPE, parameterDescriptor.type.toString())
                                        val paramTemplateSplit = paramTemplate.split("{{")
                                        paramTemplateSplit.forEach {
                                            it.split("}}").forEach {
                                                when (it) {
                                                    "PARAM_VALUE" -> {
                                                        val type = parameterDescriptor.type
                                                        when {
                                                            type.isBooleanOrNullableBoolean()       -> {
                                                                visitVarInsn(Opcodes.ILOAD, ++index)
                                                                invokestatic("java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false)
                                                            }
                                                            type.isByte()                           -> {
                                                                visitVarInsn(Opcodes.ILOAD, ++index)
                                                                invokestatic("java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false)
                                                            }
                                                            type.isShort()                          -> {
                                                                visitVarInsn(Opcodes.ILOAD, ++index)
                                                                invokestatic("java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false)
                                                            }
                                                            type.isInt()                            -> {
                                                                visitVarInsn(Opcodes.ILOAD, ++index)
                                                                invokestatic("java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false)
                                                            }
                                                            type.isLong()                           -> {
                                                                visitVarInsn(Opcodes.LLOAD, ++index)
                                                                invokestatic("java/lang/Long", "valueOf", "(L)Ljava/lang/Long;", false)
                                                            }
                                                            type.isChar()                           -> {
                                                                visitVarInsn(Opcodes.ILOAD, ++index)
                                                                invokestatic("java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false)
                                                            }
                                                            type.isFloat()                          -> {
                                                                visitVarInsn(Opcodes.FLOAD, ++index)
                                                                invokestatic("java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false)
                                                            }
                                                            type.isDouble()                         -> {
                                                                visitVarInsn(Opcodes.DLOAD, ++index)
                                                                invokestatic("java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false)
                                                            }
                                                            type.toString().contains("Array", true) -> {
                                                                index += 2
                                                                visitVarInsn(Opcodes.ALOAD, index)
                                                                invokestatic("java/util/Arrays", "toString", "([Ljava/lang/Object;)Ljava/lang/String;", false)
                                                            }
                                                            else                                    -> {
                                                                visitVarInsn(Opcodes.ALOAD, ++index)
                                                                invokestatic("java/util/Objects", "toString", "(Ljava/lang/Object;)Ljava/lang/String;", false)
                                                            }
                                                        }
                                                        invokevirtual(
                                                            "java/lang/StringBuilder",
                                                            "append",
                                                            "(Ljava/lang/Object;)Ljava/lang/StringBuilder;",
                                                            false
                                                        )
                                                    }
                                                    else          -> {
                                                        visitLdcInsn(it)
                                                        invokevirtual(
                                                            "java/lang/StringBuilder",
                                                            "append",
                                                            "(Ljava/lang/Object;)Ljava/lang/StringBuilder;",
                                                            false
                                                        )
                                                    }
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
                    if (config.base == "SLF4J") {
                        invokeinterface("org/slf4j/Logger", "debug", "(Ljava/lang/String;)V")
                    } else {
                        invokevirtual("java/io/PrintStream", "println", "(Ljava/lang/String;)V", false)
                    }
                }
            }
        }
    }

    private fun checkFunctionWithDefault(function: FunctionDescriptor, default: Boolean): Boolean {
        function.parentsWithSelf.forEach {
            if (it.annotations.hasAnnotation(FqName(de.menkalian.auriga.annotations.Log::class.qualifiedName!!))) {
                return true
            }
            if (it.annotations.hasAnnotation(FqName(de.menkalian.auriga.annotations.NoLog::class.qualifiedName!!))) {
                return false
            }
        }
        return default
    }
}