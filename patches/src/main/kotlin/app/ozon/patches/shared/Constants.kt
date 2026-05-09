package app.ozon.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal object Constants {
    const val PACKAGE_NAME = "ru.ozon.app.android"

    val COMPATIBILITY_OZON = Compatibility(
        name = "Ozon",
        packageName = PACKAGE_NAME,
        apkFileType = ApkFileType.APK,
        appIconColor = 0x005BFF,
        targets = listOf(
            AppTarget(
                version = null,
                minSdk = 26,
            ),
        ),
    )
}
