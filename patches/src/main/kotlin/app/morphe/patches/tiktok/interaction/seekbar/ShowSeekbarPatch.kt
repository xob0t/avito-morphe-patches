/*
 * Forked from:
 * https://gitlab.com/ReVanced/revanced-patches/-/blob/main/patches/src/main/kotlin/app/revanced/patches/tiktok/interaction/seekbar/ShowSeekbarPatch.kt
 */
package app.morphe.patches.tiktok.interaction.seekbar

import app.morphe.patches.shared.compat.AppCompatibilities
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val showSeekbarPatch = bytecodePatch(
    name = "Show seekbar",
    description = "Shows a progress bar for all videos. (Supports TikTok 43.6.2 + 43.8.3.)",
    default = true,
) {
    compatibleWith(*AppCompatibilities.tiktok4362And4383())

    execute {
        ShouldShowSeekBarFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )

        SetSeekBarShowTypeFingerprint.method.apply {
            val typeRegister = implementation!!.registerCount - 1
            addInstructions(
                0,
                """
                    const/16 v$typeRegister, 0x64
                """,
            )
        }
    }
}

