/*
 * Forked from:
 * https://gitlab.com/ReVanced/revanced-patches/-/blob/main/patches/src/main/kotlin/app/revanced/patches/googlephotos/misc/login/SelectedAccountPatch.kt
 */
package app.morphe.patches.googlephotos.misc.login

import app.morphe.patches.shared.compat.AppCompatibilities
import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val selectedAccountPatch = bytecodePatch(
    name = "Fix selected account persistence",
    description = "Prevents Google Photos from clearing the selected account after cold start when using MicroG.",
) {
    compatibleWith(AppCompatibilities.GOOGLE_PHOTOS)

    execute {
        // 1) Disable the AccountValidityMonitor check that runs on resume.
        AccountValidityMonitorCheckFingerprint.method.addInstruction(
            0,
            "return-void",
        )

        // 2) Make the frictionless-login eligibility check always succeed.
        FrictionlessEligibilityFingerprint.method.addInstructions(
            0,
            """
                const/4 p0, 0x1
                return p0
            """,
        )
    }
}

