package app.ozon.patches.ads

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.ozon.patches.shared.Constants.COMPATIBILITY_OZON
import com.android.tools.smali.dexlib2.iface.Method

private const val OZON_AD_WIDGETS_PREFIX = "Lru/ozon/app/android/ads/widgets/"
private const val OZON_INSTALLMENT_WIDGETS_PREFIX = "Lru/ozon/app/android/pdp/widgets/installmentPurchase/"
private const val OZON_INSTALLMENT_V4_PREFIX = "Lru/ozon/app/android/pdp/widgets/installmentPurchaseV4/"
private const val OZON_INSTALLMENT_V4_DTO =
    "Lru/ozon/app/android/pdp/widgets/installmentPurchaseV4/data/InstallmentPurchaseV4DTO;"
private const val OZON_REC_SHELF_PREFIX = "Lru/ozon/app/android/fresh/unsorted/widgets/recShelf/"
private const val OZON_REC_SHELF_VIEW_MODEL =
    "Lru/ozon/app/android/fresh/unsorted/widgets/recShelf/presentation/RecShelfViewModel;"
private const val OZON_CROSS_SALE_PREFIX = "Lru/ozon/app/android/pdp/widgets/crosssale/"
private const val OZON_CMS_BANNER_CAROUSEL_PREFIX =
    "Lru/ozon/app/android/storefront/widgets/cms/bannercarousel/"
private const val OZON_TILE_SCROLL_PREFIX =
    "Lru/ozon/app/android/universalwidgets/widgets/uw/sku/tilescroll/"
private const val OZON_TILE_GRID3_PREFIX =
    "Lru/ozon/app/android/universalwidgets/widgets/uw/sku/tilegrid3/"
private const val OZON_TILE_GRID3_CONFIG =
    "Lru/ozon/app/android/universalwidgets/widgets/uw/sku/tilegrid3/data/TileGrid3Config;"
private const val OZON_SEARCH_EXPANDABLE_CELLS_PREFIX =
    "Lru/ozon/app/android/search/widgets/expandableCells/"
private const val OZON_SEARCH_WARLOCK_VIEW_MODEL =
    "Lru/ozon/app/android/search/widgets/expandableCells/presentation/GetWarlockSectionViewModelImpl;"
private const val OZON_DS_ATOMS_MAPPER =
    "Lru/ozon/app/android/widgets/designSystemAtoms/core/DsAtomsMapper;"
private const val OZON_CELL_LIST_V2_MAPPER =
    "Lru/ozon/app/android/widgets/commonTextWidget/cellList/core/CellListV2Mapper;"
private const val OZON_CELL_V2_VIEW_HOLDER =
    "Lru/ozon/app/android/widgets/commonTextWidget/cellList/presentation/CellV2ViewHolder;"

private fun Method.hasImplementation() = implementation != null

private fun Method.isWidgetCanMapMethod() =
    name == "canMap" &&
        returnType == "Z" &&
        parameterTypes.size == 1 &&
        hasImplementation()

private fun Method.isListMapMethod() =
    (name == "map" || name == "invoke") &&
        returnType == "Ljava/util/List;" &&
        hasImplementation()

private fun Method.isViewHolderBindMethod(classType: String) =
    name == "bind" &&
        returnType == "V" &&
        hasImplementation() &&
        (classType.contains("ViewHolder") || classType.endsWith("VH;"))

private fun Method.isRecShelfRequestMethod(classType: String) =
    classType == OZON_REC_SHELF_VIEW_MODEL &&
        name == "requestRecs" &&
        returnType == "V" &&
        parameterTypes.size == 2 &&
        hasImplementation()

private fun Method.isInstallmentV4ParserMethod() =
    name == "invoke" &&
        returnType == OZON_INSTALLMENT_V4_DTO &&
        parameterTypes.size == 1 &&
        hasImplementation()

private fun Method.isTileGrid3ParseMethod(classType: String) =
    classType == OZON_TILE_GRID3_CONFIG &&
        name == "parse" &&
        returnType == "Ljava/util/List;" &&
        parameterTypes.size == 1 &&
        hasImplementation()

private fun Method.isSearchWarlockRequestMethod(classType: String) =
    classType == OZON_SEARCH_WARLOCK_VIEW_MODEL &&
        name == "getWarlockSection" &&
        returnType == "V" &&
        parameterTypes.size == 3 &&
        hasImplementation()

private fun Method.isDesignSystemAtomsMapperInvoke(classType: String) =
    classType == OZON_DS_ATOMS_MAPPER &&
        name == "invoke" &&
        returnType == "Ljava/util/List;" &&
        parameterTypes.size == 2 &&
        hasImplementation()

private fun Method.isCellListV2MapperInvoke(classType: String) =
    classType == OZON_CELL_LIST_V2_MAPPER &&
        name == "invoke" &&
        returnType == "Ljava/util/List;" &&
        parameterTypes.size == 2 &&
        hasImplementation()

private fun Method.isCellV2ViewHolderBind(classType: String) =
    classType == OZON_CELL_V2_VIEW_HOLDER &&
        name == "bind" &&
        returnType == "V" &&
        parameterTypes.size == 2 &&
        hasImplementation()

@Suppress("unused")
val removeOzonAdsPatch = bytecodePatch(
    name = "Remove Ozon ads",
    description = "Removes Ozon ad widgets, banner carousels, video ads, and PDP promo blocks.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_OZON)

    execute {
        var patchedAdCanMapMethods = 0
        var patchedAdListMapMethods = 0
        var patchedAdBindMethods = 0
        var patchedInstallmentCanMapMethods = 0
        var patchedInstallmentListMapMethods = 0
        var patchedInstallmentBindMethods = 0
        var patchedInstallmentV4ParserMethods = 0
        var patchedRecShelfCanMapMethods = 0
        var patchedRecShelfListMapMethods = 0
        var patchedRecShelfBindMethods = 0
        var patchedRecShelfRequestMethods = 0
        var patchedCrossSaleListMapMethods = 0
        var patchedCrossSaleBindMethods = 0
        var patchedCmsBannerListMapMethods = 0
        var patchedCmsBannerBindMethods = 0
        var patchedTileScrollListMapMethods = 0
        var patchedTileScrollBindMethods = 0
        var patchedTileGrid3CanMapMethods = 0
        var patchedTileGrid3ListMapMethods = 0
        var patchedTileGrid3BindMethods = 0
        var patchedTileGrid3ParseMethods = 0
        var patchedSearchExpandableCanMapMethods = 0
        var patchedSearchExpandableListMapMethods = 0
        var patchedSearchExpandableBindMethods = 0
        var patchedSearchWarlockRequestMethods = 0
        var patchedDesignSystemAtomMapperMethods = 0
        var patchedCellListV2MapperMethods = 0
        var patchedCellV2BindMethods = 0

        classDefForEach { classDef ->
            val classType = classDef.type

            when {
                classType == OZON_CELL_V2_VIEW_HOLDER -> {
                    mutableClassDefBy(classDef).methods.forEach { method ->
                        if (method.isCellV2ViewHolderBind(classType)) {
                            method.addInstructions(
                                0,
                                """
                                    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
                                    move-result-object v0
                                    const-string v1, "FIRST15"
                                    invoke-virtual {v0, v1}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z
                                    move-result v0
                                    if-eqz v0, :ozon_cell_v2_continue
                                    iget-object v0, p0, Landroidx/recyclerview/widget/RecyclerView${'$'}ViewHolder;->itemView:Landroid/view/View;
                                    invoke-virtual {v0}, Landroid/view/View;->getLayoutParams()Landroid/view/ViewGroup${'$'}LayoutParams;
                                    move-result-object v1
                                    if-eqz v1, :ozon_cell_v2_hidden_return
                                    const/4 v0, 0x0
                                    iput v0, v1, Landroid/view/ViewGroup${'$'}LayoutParams;->height:I
                                    :ozon_cell_v2_hidden_return
                                    return-void
                                    :ozon_cell_v2_continue
                                """,
                            )
                            patchedCellV2BindMethods++
                        }
                    }
                }

                classType == OZON_CELL_LIST_V2_MAPPER -> {
                    mutableClassDefBy(classDef).methods.forEach { method ->
                        if (method.isCellListV2MapperInvoke(classType)) {
                            method.addInstructions(
                                0,
                                """
                                    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
                                    move-result-object v0
                                    const-string v1, "generic-warlock"
                                    invoke-virtual {v0, v1}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z
                                    move-result v0
                                    if-eqz v0, :ozon_cell_list_continue
                                    invoke-static {}, Ljava/util/Collections;->emptyList()Ljava/util/List;
                                    move-result-object v0
                                    return-object v0
                                    :ozon_cell_list_continue
                                """,
                            )
                            patchedCellListV2MapperMethods++
                        }
                    }
                }

                classType == OZON_DS_ATOMS_MAPPER -> {
                    mutableClassDefBy(classDef).methods.forEach { method ->
                        if (method.isDesignSystemAtomsMapperInvoke(classType)) {
                            method.addInstructions(
                                0,
                                """
                                    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
                                    move-result-object v0
                                    const-string v1, "generic-warlock"
                                    invoke-virtual {v0, v1}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z
                                    move-result v0
                                    if-eqz v0, :ozon_ds_atom_continue
                                    invoke-static {}, Ljava/util/Collections;->emptyList()Ljava/util/List;
                                    move-result-object v0
                                    return-object v0
                                    :ozon_ds_atom_continue
                                """,
                            )
                            patchedDesignSystemAtomMapperMethods++
                        }
                    }
                }

                classType.startsWith(OZON_AD_WIDGETS_PREFIX) -> {
                    mutableClassDefBy(classDef).methods.forEach { method ->
                        when {
                            method.isWidgetCanMapMethod() -> {
                                method.addInstructions(
                                    0,
                                    """
                                        const/4 p0, 0x0
                                        return p0
                                    """,
                                )
                                patchedAdCanMapMethods++
                            }

                            method.isListMapMethod() -> {
                                method.addInstructions(
                                    0,
                                    """
                                        invoke-static {}, Ljava/util/Collections;->emptyList()Ljava/util/List;
                                        move-result-object p0
                                        return-object p0
                                    """,
                                )
                                patchedAdListMapMethods++
                            }

                            method.isViewHolderBindMethod(classType) -> {
                                method.addInstructions(
                                    0,
                                    """
                                        iget-object p0, p0, Landroidx/recyclerview/widget/RecyclerView${'$'}ViewHolder;->itemView:Landroid/view/View;
                                        invoke-virtual {p0}, Landroid/view/View;->getLayoutParams()Landroid/view/ViewGroup${'$'}LayoutParams;
                                        move-result-object p1
                                        if-eqz p1, :ozon_hidden_return
                                        const/4 p0, 0x0
                                        iput p0, p1, Landroid/view/ViewGroup${'$'}LayoutParams;->height:I
                                        :ozon_hidden_return
                                        return-void
                                    """,
                                )
                                patchedAdBindMethods++
                            }
                        }
                    }
                }

                classType.startsWith(OZON_INSTALLMENT_WIDGETS_PREFIX) -> {
                    mutableClassDefBy(classDef).methods.forEach { method ->
                        when {
                            method.isWidgetCanMapMethod() -> {
                                method.addInstructions(
                                    0,
                                    """
                                        const/4 p0, 0x0
                                        return p0
                                    """,
                                )
                                patchedInstallmentCanMapMethods++
                            }

                            method.isListMapMethod() -> {
                                method.addInstructions(
                                    0,
                                    """
                                        invoke-static {}, Ljava/util/Collections;->emptyList()Ljava/util/List;
                                        move-result-object p0
                                        return-object p0
                                    """,
                                )
                                patchedInstallmentListMapMethods++
                            }

                            method.isViewHolderBindMethod(classType) -> {
                                method.addInstructions(
                                    0,
                                    """
                                        iget-object p0, p0, Landroidx/recyclerview/widget/RecyclerView${'$'}ViewHolder;->itemView:Landroid/view/View;
                                        invoke-virtual {p0}, Landroid/view/View;->getLayoutParams()Landroid/view/ViewGroup${'$'}LayoutParams;
                                        move-result-object p1
                                        if-eqz p1, :ozon_hidden_return
                                        const/4 p0, 0x0
                                        iput p0, p1, Landroid/view/ViewGroup${'$'}LayoutParams;->height:I
                                        :ozon_hidden_return
                                        return-void
                                    """,
                                )
                                patchedInstallmentBindMethods++
                            }
                        }
                    }
                }

                classType.startsWith(OZON_INSTALLMENT_V4_PREFIX) -> {
                    mutableClassDefBy(classDef).methods.forEach { method ->
                        if (method.isInstallmentV4ParserMethod()) {
                            method.addInstructions(
                                0,
                                """
                                    const/4 p0, 0x0
                                    return-object p0
                                """,
                            )
                            patchedInstallmentV4ParserMethods++
                        }
                    }
                }

                classType.startsWith(OZON_REC_SHELF_PREFIX) -> {
                    mutableClassDefBy(classDef).methods.forEach { method ->
                        when {
                            method.isWidgetCanMapMethod() -> {
                                method.addInstructions(
                                    0,
                                    """
                                        const/4 p0, 0x0
                                        return p0
                                    """,
                                )
                                patchedRecShelfCanMapMethods++
                            }

                            method.isListMapMethod() -> {
                                method.addInstructions(
                                    0,
                                    """
                                        invoke-static {}, Ljava/util/Collections;->emptyList()Ljava/util/List;
                                        move-result-object p0
                                        return-object p0
                                    """,
                                )
                                patchedRecShelfListMapMethods++
                            }

                            method.isViewHolderBindMethod(classType) -> {
                                method.addInstructions(
                                    0,
                                    """
                                        iget-object p0, p0, Landroidx/recyclerview/widget/RecyclerView${'$'}ViewHolder;->itemView:Landroid/view/View;
                                        invoke-virtual {p0}, Landroid/view/View;->getLayoutParams()Landroid/view/ViewGroup${'$'}LayoutParams;
                                        move-result-object p1
                                        if-eqz p1, :ozon_hidden_return
                                        const/4 p0, 0x0
                                        iput p0, p1, Landroid/view/ViewGroup${'$'}LayoutParams;->height:I
                                        :ozon_hidden_return
                                        return-void
                                    """,
                                )
                                patchedRecShelfBindMethods++
                            }

                            method.isRecShelfRequestMethod(classType) -> {
                                method.addInstructions(0, "return-void")
                                patchedRecShelfRequestMethods++
                            }
                        }
                    }
                }

                classType.startsWith(OZON_CROSS_SALE_PREFIX) -> {
                    mutableClassDefBy(classDef).methods.forEach { method ->
                        when {
                            method.isListMapMethod() -> {
                                method.addInstructions(
                                    0,
                                    """
                                        invoke-static {}, Ljava/util/Collections;->emptyList()Ljava/util/List;
                                        move-result-object p0
                                        return-object p0
                                    """,
                                )
                                patchedCrossSaleListMapMethods++
                            }

                            method.isViewHolderBindMethod(classType) -> {
                                method.addInstructions(
                                    0,
                                    """
                                        iget-object p0, p0, Landroidx/recyclerview/widget/RecyclerView${'$'}ViewHolder;->itemView:Landroid/view/View;
                                        invoke-virtual {p0}, Landroid/view/View;->getLayoutParams()Landroid/view/ViewGroup${'$'}LayoutParams;
                                        move-result-object p1
                                        if-eqz p1, :ozon_hidden_return
                                        const/4 p0, 0x0
                                        iput p0, p1, Landroid/view/ViewGroup${'$'}LayoutParams;->height:I
                                        :ozon_hidden_return
                                        return-void
                                    """,
                                )
                                patchedCrossSaleBindMethods++
                            }
                        }
                    }
                }

                classType.startsWith(OZON_CMS_BANNER_CAROUSEL_PREFIX) -> {
                    mutableClassDefBy(classDef).methods.forEach { method ->
                        when {
                            method.isListMapMethod() -> {
                                method.addInstructions(
                                    0,
                                    """
                                        invoke-static {}, Ljava/util/Collections;->emptyList()Ljava/util/List;
                                        move-result-object p0
                                        return-object p0
                                    """,
                                )
                                patchedCmsBannerListMapMethods++
                            }

                            method.isViewHolderBindMethod(classType) -> {
                                method.addInstructions(
                                    0,
                                    """
                                        iget-object p0, p0, Landroidx/recyclerview/widget/RecyclerView${'$'}ViewHolder;->itemView:Landroid/view/View;
                                        invoke-virtual {p0}, Landroid/view/View;->getLayoutParams()Landroid/view/ViewGroup${'$'}LayoutParams;
                                        move-result-object p1
                                        if-eqz p1, :ozon_hidden_return
                                        const/4 p0, 0x0
                                        iput p0, p1, Landroid/view/ViewGroup${'$'}LayoutParams;->height:I
                                        :ozon_hidden_return
                                        return-void
                                    """,
                                )
                                patchedCmsBannerBindMethods++
                            }
                        }
                    }
                }

                classType.startsWith(OZON_TILE_SCROLL_PREFIX) -> {
                    mutableClassDefBy(classDef).methods.forEach { method ->
                        when {
                            method.isListMapMethod() -> {
                                method.addInstructions(
                                    0,
                                    """
                                        invoke-static {}, Ljava/util/Collections;->emptyList()Ljava/util/List;
                                        move-result-object p0
                                        return-object p0
                                    """,
                                )
                                patchedTileScrollListMapMethods++
                            }

                            method.isViewHolderBindMethod(classType) -> {
                                method.addInstructions(
                                    0,
                                    """
                                        iget-object p0, p0, Landroidx/recyclerview/widget/RecyclerView${'$'}ViewHolder;->itemView:Landroid/view/View;
                                        invoke-virtual {p0}, Landroid/view/View;->getLayoutParams()Landroid/view/ViewGroup${'$'}LayoutParams;
                                        move-result-object p1
                                        if-eqz p1, :ozon_hidden_return
                                        const/4 p0, 0x0
                                        iput p0, p1, Landroid/view/ViewGroup${'$'}LayoutParams;->height:I
                                        :ozon_hidden_return
                                        return-void
                                    """,
                                )
                                patchedTileScrollBindMethods++
                            }
                        }
                    }
                }

                classType.startsWith(OZON_TILE_GRID3_PREFIX) -> {
                    mutableClassDefBy(classDef).methods.forEach { method ->
                        when {
                            method.isTileGrid3ParseMethod(classType) -> {
                                method.addInstructions(
                                    0,
                                    """
                                        invoke-static {}, Ljava/util/Collections;->emptyList()Ljava/util/List;
                                        move-result-object p0
                                        return-object p0
                                    """,
                                )
                                patchedTileGrid3ParseMethods++
                            }

                            method.isWidgetCanMapMethod() -> {
                                method.addInstructions(
                                    0,
                                    """
                                        const/4 p0, 0x0
                                        return p0
                                    """,
                                )
                                patchedTileGrid3CanMapMethods++
                            }

                            method.isListMapMethod() -> {
                                method.addInstructions(
                                    0,
                                    """
                                        invoke-static {}, Ljava/util/Collections;->emptyList()Ljava/util/List;
                                        move-result-object p0
                                        return-object p0
                                    """,
                                )
                                patchedTileGrid3ListMapMethods++
                            }

                            method.isViewHolderBindMethod(classType) -> {
                                method.addInstructions(
                                    0,
                                    """
                                        iget-object p0, p0, Landroidx/recyclerview/widget/RecyclerView${'$'}ViewHolder;->itemView:Landroid/view/View;
                                        invoke-virtual {p0}, Landroid/view/View;->getLayoutParams()Landroid/view/ViewGroup${'$'}LayoutParams;
                                        move-result-object p1
                                        if-eqz p1, :ozon_hidden_return
                                        const/4 p0, 0x0
                                        iput p0, p1, Landroid/view/ViewGroup${'$'}LayoutParams;->height:I
                                        :ozon_hidden_return
                                        return-void
                                    """,
                                )
                                patchedTileGrid3BindMethods++
                            }
                        }
                    }
                }

                classType.startsWith(OZON_SEARCH_EXPANDABLE_CELLS_PREFIX) -> {
                    mutableClassDefBy(classDef).methods.forEach { method ->
                        when {
                            method.isSearchWarlockRequestMethod(classType) -> {
                                method.addInstructions(0, "return-void")
                                patchedSearchWarlockRequestMethods++
                            }

                            method.isWidgetCanMapMethod() -> {
                                method.addInstructions(
                                    0,
                                    """
                                        const/4 p0, 0x0
                                        return p0
                                    """,
                                )
                                patchedSearchExpandableCanMapMethods++
                            }

                            method.isListMapMethod() -> {
                                method.addInstructions(
                                    0,
                                    """
                                        invoke-static {}, Ljava/util/Collections;->emptyList()Ljava/util/List;
                                        move-result-object p0
                                        return-object p0
                                    """,
                                )
                                patchedSearchExpandableListMapMethods++
                            }

                            method.isViewHolderBindMethod(classType) -> {
                                method.addInstructions(
                                    0,
                                    """
                                        iget-object p0, p0, Landroidx/recyclerview/widget/RecyclerView${'$'}ViewHolder;->itemView:Landroid/view/View;
                                        invoke-virtual {p0}, Landroid/view/View;->getLayoutParams()Landroid/view/ViewGroup${'$'}LayoutParams;
                                        move-result-object p1
                                        if-eqz p1, :ozon_hidden_return
                                        const/4 p0, 0x0
                                        iput p0, p1, Landroid/view/ViewGroup${'$'}LayoutParams;->height:I
                                        :ozon_hidden_return
                                        return-void
                                    """,
                                )
                                patchedSearchExpandableBindMethods++
                            }
                        }
                    }
                }
            }
        }

        if (
            patchedAdCanMapMethods == 0 &&
            patchedAdListMapMethods == 0 &&
            patchedAdBindMethods == 0 &&
            patchedInstallmentCanMapMethods == 0 &&
            patchedInstallmentListMapMethods == 0 &&
            patchedInstallmentBindMethods == 0 &&
            patchedInstallmentV4ParserMethods == 0 &&
            patchedRecShelfCanMapMethods == 0 &&
            patchedRecShelfListMapMethods == 0 &&
            patchedRecShelfBindMethods == 0 &&
            patchedRecShelfRequestMethods == 0 &&
            patchedCrossSaleListMapMethods == 0 &&
            patchedCrossSaleBindMethods == 0 &&
            patchedCmsBannerListMapMethods == 0 &&
            patchedCmsBannerBindMethods == 0 &&
            patchedTileScrollListMapMethods == 0 &&
            patchedTileScrollBindMethods == 0 &&
            patchedTileGrid3CanMapMethods == 0 &&
            patchedTileGrid3ListMapMethods == 0 &&
            patchedTileGrid3BindMethods == 0 &&
            patchedTileGrid3ParseMethods == 0 &&
            patchedSearchExpandableCanMapMethods == 0 &&
            patchedSearchExpandableListMapMethods == 0 &&
            patchedSearchExpandableBindMethods == 0 &&
            patchedSearchWarlockRequestMethods == 0 &&
            patchedDesignSystemAtomMapperMethods == 0 &&
            patchedCellListV2MapperMethods == 0 &&
            patchedCellV2BindMethods == 0
        ) {
            throw PatchException("No Ozon ad, installment, or recommendation widget methods were found")
        }

        println(
            "Remove Ozon ads: patched $patchedAdCanMapMethods ad canMap methods, " +
                "$patchedAdListMapMethods ad list map methods, " +
                "$patchedAdBindMethods ad bind methods, " +
                "$patchedInstallmentCanMapMethods installment canMap methods, and " +
                "$patchedInstallmentListMapMethods installment list map methods, " +
                "$patchedInstallmentBindMethods installment bind methods, " +
                "$patchedInstallmentV4ParserMethods installment V4 parser methods, " +
                "$patchedRecShelfCanMapMethods recommendation canMap methods, " +
                "$patchedRecShelfListMapMethods recommendation list map methods, " +
                "$patchedRecShelfBindMethods recommendation bind methods, and " +
                "$patchedRecShelfRequestMethods recommendation request methods, " +
                "$patchedCrossSaleListMapMethods cross-sale list map methods, and " +
                "$patchedCrossSaleBindMethods cross-sale bind methods, " +
                "$patchedCmsBannerListMapMethods CMS banner list map methods, and " +
                "$patchedCmsBannerBindMethods CMS banner bind methods, " +
                "$patchedTileScrollListMapMethods tile scroll list map methods, and " +
                "$patchedTileScrollBindMethods tile scroll bind methods, " +
                "$patchedTileGrid3CanMapMethods tile grid3 canMap methods, " +
                "$patchedTileGrid3ListMapMethods tile grid3 list map methods, " +
                "$patchedTileGrid3BindMethods tile grid3 bind methods, " +
                "$patchedTileGrid3ParseMethods tile grid3 parse methods, " +
                "$patchedSearchExpandableCanMapMethods search expandable canMap methods, " +
                "$patchedSearchExpandableListMapMethods search expandable list map methods, and " +
                "$patchedSearchExpandableBindMethods search expandable bind methods, " +
                "$patchedSearchWarlockRequestMethods search Warlock request methods, " +
                "$patchedDesignSystemAtomMapperMethods design-system atom mapper methods, and " +
                "$patchedCellListV2MapperMethods cell-list V2 mapper methods, and " +
                "$patchedCellV2BindMethods cell V2 bind methods.",
        )
    }
}
