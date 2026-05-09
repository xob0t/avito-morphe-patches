package app.avito.patches.privacy

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string

private fun isAvitoClickstreamTracker(classType: String) =
    classType.startsWith("Lcom/avito/android/analytics/clickstream/")

private fun isAvitoAdjustWrapper(classType: String) =
    classType.startsWith("Lcom/avito/android/analytics_adjust/")

object ClickstreamEnqueueRunnableFingerprint : Fingerprint(
    returnType = "V",
    filters = listOf(
        string("Sending event on main thread. May cause ANR"),
        methodCall(
            definingClass = "Lcom/avito/android/analytics/inhouse_transport/u;",
            name = "add",
        ),
    ),
    custom = { _, classDef ->
        isAvitoClickstreamTracker(classDef.type)
    },
)

object AdjustInitFingerprint : Fingerprint(
    returnType = "V",
    filters = listOf(
        methodCall(
            definingClass = "Lcom/adjust/sdk/Adjust;",
            name = "initSdk",
        ),
        string("Adjust initialized"),
    ),
    custom = { _, classDef ->
        isAvitoAdjustWrapper(classDef.type)
    },
)

object AdjustTrackEventFingerprint : Fingerprint(
    returnType = "V",
    parameters = listOf(
        "Lcom/adjust/sdk/AdjustEvent;",
    ),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/adjust/sdk/Adjust;",
            name = "trackEvent",
        ),
    ),
    custom = { _, classDef ->
        isAvitoAdjustWrapper(classDef.type)
    },
)

object AdjustUserIdFingerprint : Fingerprint(
    returnType = "V",
    parameters = listOf(
        "Ljava/lang/String;",
    ),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/adjust/sdk/Adjust;",
            name = "addGlobalPartnerParameter",
        ),
        methodCall(
            definingClass = "Lcom/adjust/sdk/Adjust;",
            name = "removeGlobalPartnerParameter",
        ),
    ),
    custom = { _, classDef ->
        isAvitoAdjustWrapper(classDef.type)
    },
)

object AdjustPushTokenFingerprint : Fingerprint(
    returnType = "V",
    parameters = listOf(
        "Ljava/lang/String;",
    ),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/adjust/sdk/Adjust;",
            name = "setPushToken",
        ),
    ),
    custom = { _, classDef ->
        isAvitoAdjustWrapper(classDef.type)
    },
)
