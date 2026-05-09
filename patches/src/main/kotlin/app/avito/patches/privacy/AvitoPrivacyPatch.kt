package app.avito.patches.privacy

import app.avito.patches.shared.Constants.COMPATIBILITY_AVITO
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val avitoPrivacyPatch = bytecodePatch(
    name = "Avito privacy",
    description = "Disables Avito first-party clickstream analytics and Avito's direct Adjust telemetry wrapper.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_AVITO)

    execute {
        ClickstreamEnqueueRunnableFingerprint.method.addInstructions(
            0,
            "return-void",
        )

        listOf(
            AdjustInitFingerprint.method,
            AdjustTrackEventFingerprint.method,
            AdjustUserIdFingerprint.method,
            AdjustPushTokenFingerprint.method,
        ).forEach { method ->
            method.addInstructions(
                0,
                "return-void",
            )
        }
    }
}
