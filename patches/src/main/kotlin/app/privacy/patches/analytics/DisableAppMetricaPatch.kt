package app.privacy.patches.analytics

import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

@Suppress("unused")
val disableAppMetricaPatch = resourcePatch(
    name = "Disable AppMetrica",
    description = "Removes AppMetrica and legacy Yandex Metrica manifest entry points.",
    default = false,
) {
    execute {
        document("AndroidManifest.xml").use { document ->
            val application = document.documentElement.childrenNamed("application").single() as Element

            val appMetricaComponents = application.childrenNamed("activity", "provider", "service", "receiver")
                .filter { component ->
                    val name = component.getAttribute("android:name")
                    name.startsWith("io.appmetrica.analytics.") ||
                        name.startsWith("com.yandex.metrica.") ||
                        name.startsWith("com.yandex.preinstallsatellite.appmetrica.")
                }

            application.removeChildren(appMetricaComponents)

            val disabledComponents = application.disableComponentsWhere { name ->
                name.startsWith("io.appmetrica.analytics.") ||
                    name.startsWith("com.yandex.metrica.") ||
                    name.startsWith("com.yandex.preinstallsatellite.appmetrica.")
            }

            application.setApplicationMetaData("io.appmetrica.analytics.auto_tracking_enabled", "false")
            application.setApplicationMetaData("io.appmetrica.analytics.location_tracking_enabled", "false")

            println(
                "Disable AppMetrica: removed ${appMetricaComponents.size} and disabled " +
                    "$disabledComponents manifest components.",
            )
        }
    }
}
