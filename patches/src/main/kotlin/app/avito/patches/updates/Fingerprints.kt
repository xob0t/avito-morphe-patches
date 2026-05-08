package app.avito.patches.updates

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall

object ForceUpdateOpenFingerprint : Fingerprint(
    returnType = "V",
    parameters = listOf(
        "Lcom/avito/android/forceupdate/screens/forceupdateroot/ForceUpdateRootOpenParams;",
    ),
    filters = listOf(
        methodCall(
            definingClass = "Landroid/content/Context;",
            name = "startActivity",
        ),
    ),
    custom = { _, classDef ->
        classDef.type == "Lcom/avito/android/version_conflict/o;"
    },
)
