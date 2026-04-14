/*
 * Forked from:
 * https://gitlab.com/ReVanced/revanced-patches/-/blob/main/patches/src/main/kotlin/app/revanced/patches/tiktok/interaction/cleardisplay/RememberClearDisplayPatch.kt
 */
package app.morphe.patches.tiktok.interaction.cleardisplay

import app.morphe.patches.shared.compat.AppCompatibilities
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.tiktok.shared.OnRenderFirstFrameFingerprint
import app.morphe.util.indexOfFirstInstructionOrThrow
import app.morphe.util.returnEarly
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Suppress("unused")
val rememberClearDisplayPatch = bytecodePatch(
    name = "Remember clear display",
    description = "Remembers the clear display configurations in between videos. (Supports TikTok 43.6.2 + 43.8.3.)",
    default = true,
) {
    compatibleWith(*AppCompatibilities.tiktok4362And4383())

    execute {
        // Prevent excessive logging (can cause instability on 43.8.3).
        ClearModeLogCoreFingerprint.methodOrNull?.returnEarly()
        ClearModeLogStateFingerprint.methodOrNull?.returnEarly()
        ClearModeLogPlaytimeFingerprint.methodOrNull?.returnEarly()

        OnClearDisplayEventFingerprint.method.let { method ->
            val isEnabledIndex = method.indexOfFirstInstructionOrThrow(Opcode.IGET_BOOLEAN) + 1
            val isEnabledRegister = method.getInstruction<TwoRegisterInstruction>(isEnabledIndex - 1).registerA

            method.addInstructions(
                isEnabledIndex,
                "invoke-static {v$isEnabledRegister}, " +
                    "Lapp/morphe/extension/tiktok/cleardisplay/RememberClearDisplayPatch;->rememberClearDisplayState(Z)V",
            )

            val clearDisplayEventClass = method.parameters[0].type
            OnRenderFirstFrameFingerprint.method.addInstructions(
                0,
                """
                    invoke-static {}, Lapp/morphe/extension/tiktok/cleardisplay/RememberClearDisplayPatch;->getClearDisplayState()Z
                    move-result v1

                    if-eqz v1, :clear_display_disabled

                    const/4 v2, 0x0
                    const-string v3, ""
                    const-string v4, "long_press"

                    new-instance v0, $clearDisplayEventClass
                    invoke-direct {v0, v1, v2, v3, v4}, $clearDisplayEventClass-><init>(ZILjava/lang/String;Ljava/lang/String;)V
                    invoke-virtual {v0}, $clearDisplayEventClass->post()Lcom/ss/android/ugc/governance/eventbus/IEvent;

                    :clear_display_disabled
                    nop
                """,
            )
        }
    }
}

