/*
 * Forked from:
 * https://gitlab.com/ReVanced/revanced-patches/-/blob/main/patches/src/main/kotlin/app/revanced/patches/tiktok/misc/login/disablerequirement/DisableLoginRequirementPatch.kt
 */
package app.morphe.patches.tiktok.misc.login.disablerequirement

import app.morphe.patches.shared.compat.AppCompatibilities
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val disableLoginRequirementPatch = bytecodePatch(
    name = "Disable login requirement",
    description = "Disables mandatory login for TikTok. (Supports TikTok 43.6.2 + 43.8.3.)",
    default = true,
) {
    compatibleWith(*AppCompatibilities.tiktok4362And4383())

    execute {
        listOf(
            MandatoryLoginServiceFingerprint,
            MandatoryLoginService2Fingerprint,
        ).forEach { fp ->
            fp.method.addInstructions(
                0,
                """
                    const/4 v0, 0x0
                    return v0
                """,
            )
        }
    }
}

