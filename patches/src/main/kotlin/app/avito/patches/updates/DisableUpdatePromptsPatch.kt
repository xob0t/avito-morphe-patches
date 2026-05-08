package app.avito.patches.updates

import app.avito.patches.shared.Constants.COMPATIBILITY_AVITO
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val disableUpdatePromptsPatch = bytecodePatch(
    name = "Disable update prompts",
    description = "Prevents Avito's force-update screen opener from launching update screens.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_AVITO)

    execute {
        ForceUpdateOpenFingerprint.method.addInstructions(
            0,
            "return-void",
        )
    }
}
