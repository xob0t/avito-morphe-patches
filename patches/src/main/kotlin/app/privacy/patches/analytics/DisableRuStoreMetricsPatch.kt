package app.privacy.patches.analytics

import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

@Suppress("unused")
val disableRuStoreMetricsPatch = resourcePatch(
    name = "Disable RuStore metrics",
    description = "Disables RuStore metrics manifest entry points.",
    default = true,
) {
    execute {
        document("AndroidManifest.xml").use { document ->
            val application = document.documentElement.childrenNamed("application").single() as Element

            val disabledComponents = application.disableComponentsWhere { name ->
                name.startsWith("ru.rustore.sdk.metrics.")
            }

            println("Disable RuStore metrics: disabled $disabledComponents manifest components.")
        }
    }
}
