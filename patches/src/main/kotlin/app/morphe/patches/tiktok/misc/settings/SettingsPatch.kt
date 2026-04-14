/*
 * Forked from:
 * https://gitlab.com/ReVanced/revanced-patches/-/blob/main/patches/src/main/kotlin/app/revanced/patches/tiktok/misc/settings/SettingsPatch.kt
 */
package app.morphe.patches.tiktok.misc.settings

import app.morphe.patches.shared.compat.AppCompatibilities
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.smali.ExternalLabel
import app.morphe.patches.tiktok.misc.extension.sharedExtensionPatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction22c
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/morphe/extension/tiktok/settings/TikTokActivityHook;"

@Suppress("unused")
val settingsPatch = bytecodePatch(
    name = "Settings",
    description = "Adds Morphe settings to TikTok. Supported on 43.6.2 only. (Not compatible with 43.8.3.)",
    default = true,
) {
    dependsOn(sharedExtensionPatch)

    compatibleWith(*AppCompatibilities.tiktok4362())

    execute {
        val initializeSettingsMethodDescriptor =
            "$EXTENSION_CLASS_DESCRIPTOR->initialize(" +
                "Lcom/bytedance/ies/ugc/aweme/commercialize/compliance/personalization/AdPersonalizationActivity;" +
                ")Z"

        val createSettingsEntryMethodDescriptor =
            "$EXTENSION_CLASS_DESCRIPTOR->createSettingsEntry(" +
                "Ljava/lang/String;" +
                "Ljava/lang/String;" +
                ")Ljava/lang/Object;"

        fun String.toClassName(): String = substring(1, length - 1).replace("/", ".")

        val settingsButtonClass = SettingsEntryFingerprint.originalClassDef.type.toClassName()
        val settingsButtonInfoClass = SettingsEntryInfoFingerprint.originalClassDef.type.toClassName()

        // This "Settings" entry injection is unstable on some 43.6.2 builds (Compose settings redesign).
        // If fingerprints don't match, skip instead of failing the whole patch run.
        AddSettingsEntryFingerprint.methodOrNull?.let { addSettingsMethod ->
            val implementation = addSettingsMethod.implementation ?: return@let
            val markIndex = implementation.instructions.indexOfFirst {
                it.opcode == Opcode.IGET_OBJECT &&
                    (it as? Instruction22c)?.reference?.let { ref -> ref is FieldReference && ref.name == "headerUnit" } == true
            }

            if (markIndex < 0) return@let

            val getUnitManager = addSettingsMethod.getInstruction(markIndex + 2)
            val addEntry = addSettingsMethod.getInstruction(markIndex + 1)

            addSettingsMethod.addInstructions(markIndex + 2, listOf(getUnitManager, addEntry))

            addSettingsMethod.addInstructions(
                markIndex + 2,
                """
                    const-string v0, "$settingsButtonClass"
                    const-string v1, "$settingsButtonInfoClass"
                    invoke-static {v0, v1}, $createSettingsEntryMethodDescriptor
                    move-result-object v0
                    check-cast v0, ${SettingsEntryFingerprint.originalClassDef.type}
                """,
            )
        }

        AdPersonalizationActivityOnCreateFingerprint.methodOrNull?.let { activityOnCreate ->
            val implementation = activityOnCreate.implementation ?: return@let
            val initializeSettingsIndex = implementation.instructions.indexOfFirst { it.opcode == Opcode.INVOKE_SUPER } + 1
            if (initializeSettingsIndex <= 0) return@let

            val thisRegister = activityOnCreate.getInstruction<Instruction35c>(initializeSettingsIndex - 1).registerC
            val usableRegister = implementation.registerCount - activityOnCreate.parameterTypes.size - 2

            activityOnCreate.addInstructionsWithLabels(
                initializeSettingsIndex,
                """
                    invoke-static {v$thisRegister}, $initializeSettingsMethodDescriptor
                    move-result v$usableRegister
                    if-eqz v$usableRegister, :do_not_open
                    return-void
                """,
                ExternalLabel("do_not_open", activityOnCreate.getInstruction(initializeSettingsIndex)),
            )
        }
    }
}

