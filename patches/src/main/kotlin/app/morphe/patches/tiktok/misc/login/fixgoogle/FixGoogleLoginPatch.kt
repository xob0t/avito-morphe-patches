/*
 * Forked from:
 * https://gitlab.com/ReVanced/revanced-patches/-/blob/main/patches/src/main/kotlin/app/revanced/patches/tiktok/misc/login/fixgoogle/FixGoogleLoginPatch.kt
 */
package app.morphe.patches.tiktok.misc.login.fixgoogle

import app.morphe.patches.shared.compat.AppCompatibilities
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val fixGoogleLoginPatch = bytecodePatch(
    name = "Fix Google login",
    description = "Allows logging in with a Google account. (Supports TikTok 43.6.2 + 43.8.3.)",
    default = true,
) {
    compatibleWith(*AppCompatibilities.tiktok4362And4383())

    execute {
        listOf(
            GoogleOneTapAuthAvailableFingerprint.method,
            GoogleAuthAvailableFingerprint.method,
        ).forEach { method ->
            method.addInstructions(
                0,
                """
                    const/4 v0, 0x0
                    return v0
                """,
            )
        }
    }
}

