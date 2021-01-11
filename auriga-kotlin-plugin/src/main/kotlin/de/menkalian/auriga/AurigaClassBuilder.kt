package de.menkalian.auriga

import de.menkalian.auriga.config.AurigaConfig
import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.codegen.DelegatingClassBuilder
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter

class AurigaClassBuilder(val config: AurigaConfig, val delegateBuilder: ClassBuilder) : DelegatingClassBuilder() {
    override fun getDelegate(): ClassBuilder = delegateBuilder

    init {
        org.jetbrains.kotlin.konan.file.createTempFile("AURIGA", ".txt").writeText("${delegateBuilder.thisName}\n$config")
    }

    override fun newMethod(origin: JvmDeclarationOrigin, access: Int, name: String, desc: String, signature: String?, exceptions: Array<out String>?
    ): MethodVisitor {
        val original = super.newMethod(origin, access, name, desc, signature, exceptions)
        val function = origin.descriptor as? FunctionDescriptor ?: return original
        org.jetbrains.kotlin.konan.file.createTempFile(function.name.toString(), ".method.txt").writeText(function.toString())
        return object : MethodVisitor(Opcodes.ASM5, original) {
            override fun visitCode() {
                super.visitCode()
                InstructionAdapter(this).apply {
                    TODO("on method entry")
                }
            }

            override fun visitInsn(opcode: Int) {
                when (opcode) {
                    Opcodes.RETURN, Opcodes.ARETURN, Opcodes.IRETURN -> {
                        InstructionAdapter(this).apply { TODO("on method exit") }
                    }
                }
                super.visitInsn(opcode)
            }
        }
    }
}