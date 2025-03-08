package com.github.jikoo.openinv.specialsource

import net.md_5.specialsource.NodeType
import net.md_5.specialsource.RemapperProcessor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodInsnNode

open class ReflectionPreprocessor(
  private val jarMapping: ReflectionJarMapping
) : RemapperProcessor(null, jarMapping, null) {

  override fun process(classReader: ClassReader): ByteArray {
    val classNode = ClassNode()
    classReader.accept(classNode, 0)

    for (methodNode in classNode.methods) {
      var insn = methodNode.instructions.first
      while (insn != null) {
        when (insn.opcode) {
          Opcodes.INVOKEVIRTUAL -> this.remapGetDeclaredField(insn)
          Opcodes.INVOKESTATIC -> this.remapClassForName(insn)
        }
        insn = insn.next
      }
    }

    val classWriter = ClassWriter(0)
    classNode.accept(classWriter)
    return classWriter.toByteArray()
  }

  open fun remapGetDeclaredField(insn: AbstractInsnNode) {
    val mi = insn as MethodInsnNode

    if (mi.owner != "java/lang/Class" || mi.name != "getDeclaredField" || mi.desc != "(Ljava/lang/String;)Ljava/lang/reflect/Field;") {
      return
    }

    this.logR("Found getDeclaredField!")
    if (insn.previous == null || insn.previous.opcode != Opcodes.LDC) {
      this.logR("- not constant field; skipping, prev=" + insn.getPrevious())
      return
    }
    val ldcField = insn.getPrevious() as LdcInsnNode
    if (ldcField.cst !is String) {
      this.logR("- not field string; skipping: ${ldcField.cst}")
      return
    }
    val fieldName = ldcField.cst as String
    if (ldcField.previous == null || ldcField.previous.opcode != Opcodes.LDC) {
      this.logR("- not constant class; skipping: field=$fieldName")
      return
    }
    val ldcClass = ldcField.previous as LdcInsnNode
    if (ldcClass.cst !is Type) {
      this.logR("- not class type; skipping: field=${ldcClass.cst}, class=${ldcClass.cst}")
      return
    }

    val className = (ldcClass.cst as Type).internalName
    val newName = jarMapping.tryClimb(jarMapping.reflectableFields, NodeType.FIELD, className, fieldName, null, 0)
    this.logR("Remapping $className/$fieldName -> $newName")
    if (newName != null) {
      ldcField.cst = newName
    }
  }

  open fun remapClassForName(insn: AbstractInsnNode) {
    val mi = insn as MethodInsnNode

    if (mi.owner != "java/lang/Class" || mi.name != "forName" || mi.desc != "(Ljava/lang/String;)Ljava/lang/Class;") {
      return
    }
    this.logR("Found Class forName!")
    if (insn.getPrevious() == null || insn.getPrevious().opcode != Opcodes.LDC) {
      this.logR("- not constant field; skipping, prev=${insn.previous}")
      return
    }
    val ldcClassName = insn.getPrevious() as LdcInsnNode
    if (ldcClassName.cst !is String) {
      this.logR("- not field string; skipping: " + ldcClassName.cst)
      return
    }

    val className = ldcClassName.cst as String
    val newName = jarMapping.classes[className.replace('.', '/')]
    this.logR("Remapping $className -> $newName")
    if (newName != null) {
      ldcClassName.cst = newName.replace('/', '.')
    }
  }

  protected fun logR(message: String) {
    if (this.debug) {
      println("[ReflectionRemapper] $message")
    }
  }

}
