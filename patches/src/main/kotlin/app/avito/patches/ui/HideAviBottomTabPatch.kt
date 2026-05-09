package app.avito.patches.ui

import app.avito.patches.shared.Constants.COMPATIBILITY_AVITO
import app.morphe.patcher.extensions.InstructionExtensions.instructionsOrNull
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference

private const val NAVIGATION_TAB = "Lcom/avito/android/bottom_navigation/NavigationTab;"
private const val BOTTOM_NAVIGATION_SPACE = "Lcom/avito/android/bottom_navigation/space/BottomNavigationSpace;"

private val AVI_TAB_NAMES = setOf("AI_ASSISTANT", "AI_ASSISTANT_SELLER")

private fun Instruction.fieldReferenceOrNull(): FieldReference? =
    (this as? ReferenceInstruction)?.reference as? FieldReference

private fun Instruction.stringReferenceOrNull(): String? =
    ((this as? ReferenceInstruction)?.reference as? StringReference)?.string

private fun Method.usesBottomNavigationSpace() =
    parameterTypes.any { it.toString() == BOTTOM_NAVIGATION_SPACE }

private fun Method.hasFieldReference(fields: Set<String>): Boolean =
    instructionsOrNull?.any { instruction ->
        val reference = instruction.fieldReferenceOrNull() ?: return@any false
        reference.definingClass == NAVIGATION_TAB && reference.name in fields
    } == true

@Suppress("unused")
val hideAviBottomTabPatch = bytecodePatch(
    name = "Hide Avi bottom tab",
    description = "Removes the Avi assistant button from Avito's bottom navigation bar.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_AVITO)

    execute {
        val navigationTabClass = classDefByOrNull(NAVIGATION_TAB)
            ?: throw PatchException("NavigationTab class was not found")

        val aiTabFields = navigationTabClass.methods
            .firstOrNull { it.name == "<clinit>" }
            ?.let { method ->
                val instructions = method.implementation?.instructions?.toList().orEmpty()
                buildSet {
                    instructions.forEachIndexed { index, instruction ->
                        if (instruction.stringReferenceOrNull() !in AVI_TAB_NAMES) return@forEachIndexed

                        instructions
                            .drop(index + 1)
                            .take(16)
                            .firstNotNullOfOrNull { candidate ->
                                val reference = candidate.fieldReferenceOrNull() ?: return@firstNotNullOfOrNull null
                                reference.takeIf {
                                    candidate.opcode == Opcode.SPUT_OBJECT &&
                                        it.definingClass == NAVIGATION_TAB &&
                                        it.type == NAVIGATION_TAB
                                }?.name
                            }
                            ?.let(::add)
                    }
                }
            }
            .orEmpty()

        if (aiTabFields.isEmpty()) {
            throw PatchException("Avi assistant NavigationTab fields were not found")
        }

        var patchedReferences = 0

        classDefForEach { classDef ->
            if (!classDef.type.startsWith("Lcom/avito/android/bottom_navigation/")) return@classDefForEach
            if (classDef.methods.none { it.usesBottomNavigationSpace() && it.hasFieldReference(aiTabFields) }) {
                return@classDefForEach
            }

            mutableClassDefBy(classDef).methods.forEach { method ->
                if (!method.usesBottomNavigationSpace()) return@forEach

                val instructions = method.instructionsOrNull ?: return@forEach
                instructions.toList().forEachIndexed { index, instruction ->
                    val reference = instruction.fieldReferenceOrNull() ?: return@forEachIndexed
                    if (reference.definingClass != NAVIGATION_TAB || reference.name !in aiTabFields) {
                        return@forEachIndexed
                    }

                    val targetRegister = (instruction as? OneRegisterInstruction)?.registerA
                        ?: return@forEachIndexed
                    val replacement = if (targetRegister <= 15) {
                        "const/4 v$targetRegister, 0x0"
                    } else {
                        "const/16 v$targetRegister, 0x0"
                    }

                    method.replaceInstruction(index, replacement)
                    patchedReferences++
                }
            }
        }

        if (patchedReferences == 0) {
            throw PatchException("Avi assistant bottom navigation references were not found")
        }

        println("Hide Avi bottom tab: removed $patchedReferences Avi tab references.")
    }
}
