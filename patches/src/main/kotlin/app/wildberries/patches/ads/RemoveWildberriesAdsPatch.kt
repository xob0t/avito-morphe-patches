package app.wildberries.patches.ads

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.wildberries.patches.shared.Constants.COMPATIBILITY_WILDBERRIES
import com.android.tools.smali.dexlib2.iface.Method

private const val BANNERS_UI_WRAPPER =
    "Lru/wildberries/mainpage/impl/presentation/model/BannersUiWrapper;"
private const val MAIN_BANNERS =
    "Lru/wildberries/banners/api/model/MainBanners;"
private const val MAIN_PAGE_BANNERS_CAROUSEL =
    "Lru/wildberries/mainpage/impl/presentation/compose/elements/MainPageBannersCarouselKt;"
private const val MAIN_PAGE_GRID_BANNERS =
    "Lru/wildberries/mainpage/impl/presentation/compose/elements/MainPageGridBannersKt;"
private const val MAIN_PAGE_OPTIONS =
    "Lru/wildberries/mainpage/impl/presentation/model/MainPageOptions;"
private const val MAIN_PAGE_USE_CASE_FACADE =
    "Lru/wildberries/mainpage/impl/domain/usecase/MainPageUseCaseFacade;"
private const val BIG_SALE_SEARCH_BAR_USE_CASE =
    "Lru/wildberries/mainpage/impl/domain/usecase/IsBigSaleSearchBarEnabledUseCase;"
private const val MAIN_PAGE_PREFERENCES =
    "Lru/wildberries/mainpage/impl/data/source/MainPagePreferences;"
private const val SEARCH_BAR_UI_MODEL =
    "Lru/wildberries/content/search/api/model/SearchBarUiModel;"
private const val CART_RECOMMENDATIONS_VIEW_MODEL =
    "Lru/wildberries/cart/firststep/screen/uistate/RecommendationsViewModel;"
private const val CART_SCREEN_UI_STATE =
    "Lru/wildberries/cart/firststep/screen/uistate/ProductCartUiState\$Screen;"
private const val BIG_LOTTERY_MAPPER =
    "Lru/wildberries/mainpage/impl/presentation/component/mapper/BigLotteryMapper;"
private const val BIG_LOTTERY_USE_CASE_FACADE =
    "Lru/wildberries/tickets/domain/BigLotteryUseCaseFacadeImpl;"
private const val RANDOM_TICKET_SPAWNS_USE_CASE =
    "Lru/wildberries/tickets/domain/IsRandomTicketSpawnsEnabledUseCaseImpl;"

private val nullableBannerWrapperGetters = setOf(
    "getMainBanners",
    "getMarketingBannersCarousel",
    "getSecondaryBannersCarousel",
    "getTvBannersCarousel",
)

private val listBannerWrapperGetters = setOf(
    "getGridBanners",
    "getOutBanners",
)

private val mainBannerListGetters = setOf(
    "getMainBannersCarousel",
    "getMarketingCarousel",
    "getSecondaryBannersCarousel",
    "getTvBanners",
    "getSecondSmallBannersCarousel",
    "getPromoInCatalogMenu",
    "getSearchBannersNewFormat",
    "getThanksForOrder",
    "getTvBannersCarousel",
    "getOutBanners",
    "getTvBannersCarouselNewFormat",
    "getOutBannersNewFormat",
)

private val bannerRenderMethods = setOf(
    "BannersCarousel",
    "MainPageBannersCarousel",
    "GridBanners",
    "MainPageGridBanners",
)

private fun Method.hasImplementation() = implementation != null

private fun Method.isListReturnMethod(name: String) =
    this.name == name &&
        returnType == "Ljava/util/List;" &&
        hasImplementation()

private fun Method.isBooleanMethod(name: String) =
    this.name == name &&
        returnType == "Z" &&
        hasImplementation()

private fun Method.isVoidMethod(name: String) =
    this.name == name &&
        returnType == "V" &&
        hasImplementation()

@Suppress("unused")
val removeWildberriesAdsPatch = bytecodePatch(
    name = "Remove Wildberries ads",
    description = "Removes Wildberries home banners, grid banners, promo headers, cart recommendations, and lottery popups.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_WILDBERRIES)

    execute {
        var patchedBannerWrapperNullableGetters = 0
        var patchedBannerWrapperListGetters = 0
        var patchedMainBannerListGetters = 0
        var patchedMainBannerStateMethods = 0
        var patchedBannerRenderMethods = 0
        var patchedBigSaleHeaderMethods = 0
        var patchedCartRecommendationMethods = 0
        var patchedBigLotteryMethods = 0

        classDefForEach { classDef ->
            val classType = classDef.type

            when (classType) {
                BANNERS_UI_WRAPPER -> {
                    mutableClassDefBy(classDef).methods.forEach { method ->
                        when {
                            method.name in nullableBannerWrapperGetters && method.hasImplementation() -> {
                                method.addInstructions(
                                    0,
                                    """
                                        const/4 v0, 0x0
                                        return-object v0
                                    """,
                                )
                                patchedBannerWrapperNullableGetters++
                            }

                            method.name in listBannerWrapperGetters && method.isListReturnMethod(method.name) -> {
                                method.addInstructions(
                                    0,
                                    """
                                        invoke-static {}, Ljava/util/Collections;->emptyList()Ljava/util/List;
                                        move-result-object v0
                                        return-object v0
                                    """,
                                )
                                patchedBannerWrapperListGetters++
                            }
                        }
                    }
                }

                MAIN_BANNERS -> {
                    mutableClassDefBy(classDef).methods.forEach { method ->
                        when {
                            method.name in mainBannerListGetters && method.isListReturnMethod(method.name) -> {
                                method.addInstructions(
                                    0,
                                    """
                                        invoke-static {}, Ljava/util/Collections;->emptyList()Ljava/util/List;
                                        move-result-object v0
                                        return-object v0
                                    """,
                                )
                                patchedMainBannerListGetters++
                            }

                            method.name == "isNotEmpty" &&
                                method.returnType == "Z" &&
                                method.hasImplementation() -> {
                                method.addInstructions(
                                    0,
                                    """
                                        const/4 v0, 0x0
                                        return v0
                                    """,
                                )
                                patchedMainBannerStateMethods++
                            }

                            method.name == "isVideoBannerInMainCarousel" &&
                                method.returnType == "Z" &&
                                method.hasImplementation() -> {
                                method.addInstructions(
                                    0,
                                    """
                                        const/4 v0, 0x0
                                        return v0
                                    """,
                                )
                                patchedMainBannerStateMethods++
                            }
                        }
                    }
                }

                MAIN_PAGE_BANNERS_CAROUSEL,
                MAIN_PAGE_GRID_BANNERS,
                    -> {
                    mutableClassDefBy(classDef).methods.forEach { method ->
                        if (method.name in bannerRenderMethods && method.isVoidMethod(method.name)) {
                            method.addInstructions(
                                0,
                                """
                                    return-void
                                """,
                            )
                            patchedBannerRenderMethods++
                        }
                    }
                }

                MAIN_PAGE_OPTIONS,
                MAIN_PAGE_USE_CASE_FACADE,
                BIG_SALE_SEARCH_BAR_USE_CASE,
                MAIN_PAGE_PREFERENCES,
                SEARCH_BAR_UI_MODEL,
                    -> {
                    mutableClassDefBy(classDef).methods.forEach { method ->
                        if (method.isBooleanMethod("isBigSaleSearchBarEnabled")) {
                            method.addInstructions(
                                0,
                                """
                                    const/4 v0, 0x0
                                    return v0
                                """,
                            )
                            patchedBigSaleHeaderMethods++
                        }
                    }
                }

                CART_SCREEN_UI_STATE -> {
                    mutableClassDefBy(classDef).methods.forEach { method ->
                        if (method.isBooleanMethod("getRecommendationsInEmptyCartEnabled")) {
                            method.addInstructions(
                                0,
                                """
                                    const/4 v0, 0x0
                                    return v0
                                """,
                            )
                            patchedCartRecommendationMethods++
                        }
                    }
                }

                CART_RECOMMENDATIONS_VIEW_MODEL -> {
                    mutableClassDefBy(classDef).methods.forEach { method ->
                        when {
                            method.isBooleanMethod("access\$shouldRecommendationsBeVisible") -> {
                                method.addInstructions(
                                    0,
                                    """
                                        const/4 v0, 0x0
                                        return v0
                                    """,
                                )
                                patchedCartRecommendationMethods++
                            }

                            method.isVoidMethod("loadMoreProducts") -> {
                                method.addInstructions(
                                    0,
                                    """
                                        return-void
                                    """,
                                )
                                patchedCartRecommendationMethods++
                            }

                            method.isVoidMethod("loadRecommendations") -> {
                                method.addInstructions(
                                    0,
                                    """
                                        return-void
                                    """,
                                )
                                patchedCartRecommendationMethods++
                            }
                        }
                    }
                }

                BIG_LOTTERY_MAPPER -> {
                    mutableClassDefBy(classDef).methods.forEach { method ->
                        if (method.isListReturnMethod("map")) {
                            method.addInstructions(
                                0,
                                """
                                    invoke-static {}, Ljava/util/Collections;->emptyList()Ljava/util/List;
                                    move-result-object v0
                                    return-object v0
                                """,
                            )
                            patchedBigLotteryMethods++
                        }
                    }
                }

                BIG_LOTTERY_USE_CASE_FACADE -> {
                    mutableClassDefBy(classDef).methods.forEach { method ->
                        when {
                            method.name == "isBigLotteryAvailable" &&
                                method.returnType == "Ljava/lang/Object;" &&
                                method.hasImplementation() -> {
                                method.addInstructions(
                                    0,
                                    """
                                        sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
                                        return-object v0
                                    """,
                                )
                                patchedBigLotteryMethods++
                            }

                            method.name == "access\$isBigLotteryEnabled" &&
                                method.returnType == "Z" &&
                                method.hasImplementation() -> {
                                method.addInstructions(
                                    0,
                                    """
                                        const/4 v0, 0x0
                                        return v0
                                    """,
                                )
                                patchedBigLotteryMethods++
                            }
                        }
                    }
                }

                RANDOM_TICKET_SPAWNS_USE_CASE -> {
                    mutableClassDefBy(classDef).methods.forEach { method ->
                        if (
                            method.name == "invoke" &&
                            method.returnType == "Z" &&
                            method.parameterTypes.singleOrNull()?.toString() == "Z" &&
                            method.hasImplementation()
                        ) {
                            method.addInstructions(
                                0,
                                """
                                    const/4 v0, 0x0
                                    return v0
                                """,
                            )
                            patchedBigLotteryMethods++
                        }
                    }
                }
            }
        }

        if (
            patchedBannerWrapperNullableGetters == 0 &&
            patchedBannerWrapperListGetters == 0 &&
            patchedMainBannerListGetters == 0 &&
            patchedMainBannerStateMethods == 0 &&
            patchedBannerRenderMethods == 0 &&
            patchedBigSaleHeaderMethods == 0 &&
            patchedCartRecommendationMethods == 0 &&
            patchedBigLotteryMethods == 0
        ) {
            throw PatchException("No Wildberries banner, promo header, cart recommendation, or lottery methods were found")
        }

        println(
            "Remove Wildberries ads: patched $patchedBannerWrapperNullableGetters banner wrapper object getters, " +
                "$patchedBannerWrapperListGetters banner wrapper list getters, " +
                "$patchedMainBannerListGetters main banner list getters, " +
                "$patchedMainBannerStateMethods main banner state methods, " +
                "$patchedBannerRenderMethods banner render methods, " +
                "$patchedBigSaleHeaderMethods promo header methods, and " +
                "$patchedCartRecommendationMethods cart recommendation methods, and " +
                "$patchedBigLotteryMethods lottery methods.",
        )
    }
}
