/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.codegen.intrinsics

import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.types.JetType
import org.jetbrains.kotlin.types.TypeUtils
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter
import org.jetbrains.org.objectweb.asm.tree.InsnList
import org.jetbrains.org.objectweb.asm.tree.MethodInsnNode
import org.jetbrains.org.objectweb.asm.tree.TypeInsnNode

object InstanceOf {
    private val INTRINSICS_CLASS = "kotlin/jvm/internal/Intrinsics"

    private val INSTANCEOF_METHOD_SIGNATURE = "(Ljava/lang/Object;)Z"

    private val INSTANCEOF_METHOD_NAME = hashMapOf(
            "kotlin.MutableIterator" to "isMutableIterator",
            "kotlin.MutableIterable" to "isMutableIterable",
            "kotlin.MutableCollection" to "isMutableCollection",
            "kotlin.MutableList" to "isMutableList",
            "kotlin.MutableListIterator" to "isMutableListIterator",
            "kotlin.MutableSet" to "isMutableSet",
            "kotlin.MutableMap" to "isMutableMap",
            "kotlin.MutableMap.MutableEntry" to "isMutableMapEntry"
    )

    public @JvmStatic fun instanceOf(v: InstructionAdapter, jetType: JetType, boxedAsmType: Type) {
        val intrinsicMethodName = getInstanceOfIntrinsicMethodName(jetType)
        if (intrinsicMethodName == null) {
            v.instanceOf(boxedAsmType)
        }
        else {
            v.invokestatic(INTRINSICS_CLASS, intrinsicMethodName, INSTANCEOF_METHOD_SIGNATURE, false)
        }
    }

    public @JvmStatic fun instanceOf(instanceofInsn: TypeInsnNode, instructions: InsnList, jetType: JetType, asmType: Type) {
        val intrinsicMethodName = getInstanceOfIntrinsicMethodName(jetType)
        if (intrinsicMethodName == null) {
            instanceofInsn.desc = asmType.internalName
        }
        else {
            val invokeNode = MethodInsnNode(Opcodes.INVOKESTATIC, INTRINSICS_CLASS, intrinsicMethodName, INSTANCEOF_METHOD_SIGNATURE, false)
            instructions.insertBefore(instanceofInsn, invokeNode)
            instructions.remove(instanceofInsn)
        }
    }

    private fun getInstanceOfIntrinsicMethodName(jetType: JetType): String? {
        val classDescriptor = TypeUtils.getClassDescriptor(jetType) ?: return null
        val classFqName = DescriptorUtils.getFqName(classDescriptor).asString()
        return INSTANCEOF_METHOD_NAME[classFqName]
    }
}