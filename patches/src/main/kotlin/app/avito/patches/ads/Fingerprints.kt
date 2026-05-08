package app.avito.patches.ads

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string

private fun isCommercialBannerLoader(classType: String) =
    classType.startsWith("Lcom/avito/android/advertising/loaders/")

object CommercialBannerLoaderErrorNFingerprint : Fingerprint(
    returnType = "Lio/reactivex/rxjava3/core/z;",
    filters = listOf(
        string("Not supported SerpBanner type: "),
        methodCall(
            definingClass = "Lio/reactivex/rxjava3/core/z;",
            name = "N",
        ),
    ),
    custom = { _, classDef ->
        isCommercialBannerLoader(classDef.type)
    },
)

object CommercialBannerLoaderErrorMFingerprint : Fingerprint(
    returnType = "Lio/reactivex/rxjava3/core/z;",
    filters = listOf(
        string("Not supported SerpBanner type: "),
        methodCall(
            definingClass = "Lio/reactivex/rxjava3/core/z;",
            name = "M",
        ),
    ),
    custom = { _, classDef ->
        isCommercialBannerLoader(classDef.type)
    },
)
