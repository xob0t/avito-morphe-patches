package app.tbank.patches.ads

import app.morphe.patcher.patch.resourcePatch
import app.tbank.patches.shared.Constants.COMPATIBILITY_TBANK
import org.w3c.dom.Element
import java.io.FileNotFoundException

private val storyLayoutFiles = listOf(
    "res/layout/accounts_list_fragment.xml",
    "res/layout/activity_main_flow_container.xml",
    "res/layout/payments_hub_activity.xml",
)

private val storyViewIds = setOf(
    "@id/accountsListStoriesCarouselWrapper",
    "@id/main_stories_container",
)

private const val ACCOUNT_LIST_STORIES_WRAPPER_ID = "@id/accountsListStoriesCarouselWrapper"

private val storyAppBarIds = setOf(
    "@id/accountsListCollapsingAppBarLayout",
)

private val offerLayoutFiles = listOf(
    "res/layout/core_offers_view_bottom_sheet_offer.xml",
    "res/layout/core_offers_view_combo_offer.xml",
    "res/layout/core_offers_view_huge_offer.xml",
    "res/layout/core_offers_view_in_app_message_badge_btn_offer.xml",
    "res/layout/core_offers_view_in_app_message_badge_offer.xml",
    "res/layout/core_offers_view_in_app_message_offer.xml",
    "res/layout/core_offers_view_main_offer.xml",
    "res/layout/offers_ui_view_bottom_sheet_offer.xml",
    "res/layout/offers_ui_view_combo_good_offer.xml",
    "res/layout/offers_ui_view_combo_good_redesign_offer.xml",
    "res/layout/offers_ui_view_combo_offer.xml",
    "res/layout/offers_ui_view_huge_offer.xml",
    "res/layout/offers_ui_view_in_app_message_badge_btn_offer.xml",
    "res/layout/offers_ui_view_in_app_message_badge_offer.xml",
    "res/layout/offers_ui_view_in_app_message_offer.xml",
    "res/layout/offers_ui_view_main_offer.xml",
)

private fun Element.walk(): Sequence<Element> = sequence {
    yield(this@walk)

    val nodes = childNodes
    for (index in 0 until nodes.length) {
        val child = nodes.item(index)
        if (child is Element) yieldAll(child.walk())
    }
}

private fun Element.hideView() {
    setAttribute("android:visibility", "gone")
    setAttribute("android:layout_width", "0dp")
    setAttribute("android:layout_height", "0dp")
    setAttribute("android:minHeight", "0dp")
    setAttribute("android:clickable", "false")
    setAttribute("android:focusable", "false")
    setAttribute("android:importantForAccessibility", "no")
}

private fun Element.markInvisibleViewState() {
    setAttribute("app:layout_viewState", "invisible")
}

@Suppress("unused")
val removeTBankAdsPatch = resourcePatch(
    name = "Remove TBank ads",
    description = "Removes TBank stories and promotional surfaces.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_TBANK)

    execute {
        var hiddenStoryViews = 0
        var collapsedStoryAppBars = 0
        var hiddenOfferViews = 0
        var missingLayouts = 0

        storyLayoutFiles.forEach { path ->
            try {
                document(path).use { document ->
                    document.documentElement.walk()
                        .filter { it.getAttribute("android:id") in storyViewIds }
                        .forEach { view ->
                            view.hideView()
                            if (view.getAttribute("android:id") == ACCOUNT_LIST_STORIES_WRAPPER_ID) {
                                view.markInvisibleViewState()
                            }
                            hiddenStoryViews++
                        }

                    document.documentElement.walk()
                        .filter { it.getAttribute("android:id") in storyAppBarIds }
                        .forEach { view ->
                            view.setAttribute("app:scabw_shadow_height", "0dp")
                            collapsedStoryAppBars++
                        }
                }
            } catch (_: FileNotFoundException) {
                missingLayouts++
            }
        }

        offerLayoutFiles.forEach { path ->
            try {
                document(path).use { document ->
                    document.documentElement.walk()
                        .filter { it.getAttribute("android:id") == "@id/offerContent" }
                        .forEach { view ->
                            view.hideView()
                            hiddenOfferViews++
                        }
                }
            } catch (_: FileNotFoundException) {
                missingLayouts++
            }
        }

        println(
            "Remove TBank ads: hid $hiddenStoryViews story views, " +
                "collapsed $collapsedStoryAppBars story app bars, " +
                "hid $hiddenOfferViews offer views, skipped $missingLayouts missing layouts.",
        )
    }
}
