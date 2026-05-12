package app.tbank.patches.antitamper

import app.morphe.patcher.extensions.InstructionExtensions.instructionsOrNull
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.tbank.patches.shared.Constants.COMPATIBILITY_TBANK
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference

private const val RASP_EXECUTOR = "Lcom/t/core/miaf/ndk/Executor;"
private const val SYSTEM = "Ljava/lang/System;"

// RASP native libraries to prevent loading.

private val RASP_NATIVE_LIBS = setOf(
    "i",
    "rooot",
    "toolChecker",
)

// Flag parameter names reported to backend.

private val TAMPER_FLAG_NAMES = setOf(
    "clonnedApp_flag",
    "repackagedApk_flag",
)

// Helpers.

private fun Instruction.methodReferenceOrNull(): MethodReference? =
    (this as? ReferenceInstruction)?.reference as? MethodReference

private fun Instruction.stringReferenceOrNull(): String? =
    ((this as? ReferenceInstruction)?.reference as? StringReference)?.string

private fun MethodReference.isRaspExec() =
    definingClass == RASP_EXECUTOR &&
        name == "exec" &&
        parameterTypes.size == 1 &&
        parameterTypes[0].toString() == "J" &&
        returnType == "Ljava/lang/String;"

private fun MethodReference.isRaspExec2() =
    definingClass == RASP_EXECUTOR &&
        name == "exec2" &&
        parameterTypes.size == 1 &&
        parameterTypes[0].toString() == "Z" &&
        returnType == "V"

private fun MethodReference.isSystemLoadLibrary() =
    definingClass == SYSTEM &&
        name == "loadLibrary" &&
        parameterTypes.size == 1 &&
        parameterTypes[0].toString() == "Ljava/lang/String;" &&
        returnType == "V"

@Suppress("unused")
val bypassAntiTamperPatch = bytecodePatch(
    name = "Bypass anti-tamper",
    description = "Stubs TBank's native RASP executor calls and neutralizes tamper flag reporting.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_TBANK)

    execute {
        var patchedRaspExecCalls = 0
        var patchedRaspExec2Calls = 0
        var patchedLibraryLoads = 0
        var patchedTamperFlags = 0

        classDefForEach { classDef ->
            val methods = classDef.methods.toList()

            // Check if this class has any relevant call sites.

            val hasRaspCalls = methods.any { method ->
                method.instructionsOrNull?.toList()?.any { instr ->
                    val ref = instr.methodReferenceOrNull() ?: return@any false
                    ref.isRaspExec() || ref.isRaspExec2()
                } == true
            }

            val hasLoadLibrary = methods.any { method ->
                val instructions = method.instructionsOrNull?.toList() ?: return@any false
                instructions.any { it.methodReferenceOrNull()?.isSystemLoadLibrary() == true } &&
                    instructions.any { it.stringReferenceOrNull() in RASP_NATIVE_LIBS }
            }

            val hasTamperFlag = methods.any { method ->
                method.instructionsOrNull?.toList()?.any { instr ->
                    instr.stringReferenceOrNull() in TAMPER_FLAG_NAMES
                } == true
            }

            if (!hasRaspCalls && !hasLoadLibrary && !hasTamperFlag) return@classDefForEach

            mutableClassDefBy(classDef).methods.forEach { method ->
                val instructions = method.instructionsOrNull ?: return@forEach
                val instructionList = instructions.toList()

                instructionList.forEachIndexed { index, instruction ->
                    val reference = instruction.methodReferenceOrNull()

                    when {
                        // Stub Executor.exec(long) by replacing move-result-object with an empty string.
                        reference?.isRaspExec() == true -> {
                            method.replaceInstruction(index, "nop")
                            val moveResult = instructionList.getOrNull(index + 1) as? OneRegisterInstruction
                            if (moveResult != null && moveResult.opcode == Opcode.MOVE_RESULT_OBJECT) {
                                method.replaceInstruction(
                                    index + 1,
                                    "const-string v${moveResult.registerA}, \"\"",
                                )
                            }
                            patchedRaspExecCalls++
                        }

                        // Stub Executor.exec2(boolean).
                        reference?.isRaspExec2() == true -> {
                            method.replaceInstruction(index, "nop")
                            patchedRaspExec2Calls++
                        }

                        // Block RASP native library loading.
                        reference?.isSystemLoadLibrary() == true -> {
                            for (scanIndex in index - 1 downTo maxOf(0, index - 4)) {
                                val libName = instructionList[scanIndex].stringReferenceOrNull()
                                    ?: continue
                                if (libName in RASP_NATIVE_LIBS) {
                                    method.replaceInstruction(index, "nop")
                                    patchedLibraryLoads++
                                }
                                break
                            }
                        }
                    }
                }

                // Neutralize tamper flag provider construction.
                if (hasTamperFlag) {
                    val methodHasTamperString = instructionList.any { instr ->
                        instr.stringReferenceOrNull() in TAMPER_FLAG_NAMES
                    }
                    if (methodHasTamperString) {
                        instructionList.forEachIndexed { index, instruction ->
                            when (instruction.opcode) {
                                Opcode.NEW_INSTANCE -> {
                                    method.replaceInstruction(index, "nop")
                                    patchedTamperFlags++
                                }

                                Opcode.INVOKE_DIRECT -> {
                                    method.replaceInstruction(index, "nop")
                                }

                                Opcode.CONST_STRING, Opcode.CONST_STRING_JUMBO -> {
                                    if (instruction.stringReferenceOrNull() in TAMPER_FLAG_NAMES) {
                                        method.replaceInstruction(index, "nop")
                                    }
                                }

                                Opcode.CONST_WIDE_16, Opcode.CONST_WIDE_32, Opcode.CONST_WIDE -> {
                                    method.replaceInstruction(index, "nop")
                                }

                                else -> {}
                            }
                        }
                    }
                }
            }
        }

        println(
            "Bypass anti-tamper: stubbed $patchedRaspExecCalls Executor.exec() calls, " +
                "$patchedRaspExec2Calls Executor.exec2() calls, " +
                "blocked $patchedLibraryLoads native library loads, " +
                "neutralized $patchedTamperFlags tamper flag providers.",
        )
    }
}
