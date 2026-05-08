package app.avito.patches.ads

import app.avito.patches.shared.Constants.COMPATIBILITY_AVITO
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element
import org.w3c.dom.Node

private val adPermissions = setOf(
    "com.google.android.gms.permission.AD_ID",
    "android.permission.ACCESS_ADSERVICES_ATTRIBUTION",
    "android.permission.ACCESS_ADSERVICES_AD_ID",
)

private val adComponents = setOf(
    "com.yandex.mobile.ads.common.AdActivity",
    "com.yandex.mobile.ads.core.initializer.MobileAdsInitializeProvider",
    "com.my.target.common.MyTargetActivity",
    "com.my.target.common.MyTargetContentProvider",
)

private val adProperties = setOf(
    "android.adservices.AD_SERVICES_CONFIG",
)

private val adMetaDataDefaults = mapOf(
    "google_analytics_adid_collection_enabled" to "false",
)

private val hiddenRewardLayouts = listOf(
    "res/layout/item_rewards.xml",
    "res/layout/reward_bug_entry_floating_view.xml",
)

private fun Element.childrenNamed(name: String): List<Element> {
    val nodes = childNodes
    return buildList {
        for (i in 0 until nodes.length) {
            val node = nodes.item(i)
            if (node is Element && node.nodeName == name) add(node)
        }
    }
}

private fun Element.removeChildren(nodes: List<Node>) {
    nodes.forEach(::removeChild)
}

private fun Element.getOrCreateApplicationMetaData(name: String): Element {
    childrenNamed("meta-data")
        .firstOrNull { it.getAttribute("android:name") == name }
        ?.let { return it }

    val metaData = ownerDocument.createElement("meta-data")
    metaData.setAttribute("android:name", name)
    appendChild(metaData)
    return metaData
}

private val removeAdResourcesPatch = resourcePatch {
    compatibleWith(COMPATIBILITY_AVITO)

    execute {
        document("AndroidManifest.xml").use { document ->
            val manifest = document.documentElement
            val application = manifest.childrenNamed("application").single()

            manifest.removeChildren(
                manifest.childrenNamed("uses-permission")
                    .filter { it.getAttribute("android:name") in adPermissions }
            )

            application.removeChildren(
                application.childrenNamed("property")
                    .filter { it.getAttribute("android:name") in adProperties }
            )

            listOf("activity", "provider", "service", "receiver").forEach { tag ->
                application.childrenNamed(tag)
                    .filter { it.getAttribute("android:name") in adComponents }
                    .forEach { component ->
                        component.setAttribute("android:enabled", "false")
                        component.setAttribute("android:exported", "false")
                    }
            }

            adMetaDataDefaults.forEach { (name, value) ->
                application.getOrCreateApplicationMetaData(name)
                    .setAttribute("android:value", value)
            }
        }

        hiddenRewardLayouts.forEach { path ->
            document(path).use { document ->
                document.documentElement.apply {
                    setAttribute("android:visibility", "gone")
                    setAttribute("android:layout_width", "0dp")
                    setAttribute("android:layout_height", "0dp")
                    setAttribute("android:clickable", "false")
                    setAttribute("android:focusable", "false")
                    setAttribute("android:importantForAccessibility", "no")
                }
            }
        }
    }
}

@Suppress("unused")
val removeAdsPatch = bytecodePatch(
    name = "Remove ads",
    description = "Disables Avito ads by removing ad SDK entry points and short-circuiting commercial banner loading.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_AVITO)
    dependsOn(removeAdResourcesPatch)

    execute {
        val (rxErrorFactoryName, commercialBannerLoaderMethod) = runCatching {
            "N" to CommercialBannerLoaderErrorNFingerprint.method
        }.getOrElse {
            "M" to CommercialBannerLoaderErrorMFingerprint.method
        }

        commercialBannerLoaderMethod.addInstructions(
            0,
            """
                new-instance v0, Ljava/lang/RuntimeException;
                const-string v1, "Avito ads disabled"
                invoke-direct {v0, v1}, Ljava/lang/RuntimeException;-><init>(Ljava/lang/String;)V
                invoke-static {v0}, Lio/reactivex/rxjava3/core/z;->$rxErrorFactoryName(Ljava/lang/Throwable;)Lio/reactivex/rxjava3/internal/operators/observable/V;
                move-result-object v0
                return-object v0
            """,
        )
    }
}
