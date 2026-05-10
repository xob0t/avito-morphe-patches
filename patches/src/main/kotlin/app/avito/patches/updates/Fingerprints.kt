package app.avito.patches.updates

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall

object ForceUpdateOpenFingerprint : Fingerprint(
    definingClass = "Lcom/avito/android/version_conflict/",
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
)
