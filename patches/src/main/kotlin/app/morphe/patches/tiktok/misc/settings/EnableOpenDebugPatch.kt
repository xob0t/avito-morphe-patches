/*
 * Forked from:
 * https://gitlab.com/ReVanced/revanced-patches/-/blob/main/patches/src/main/kotlin/app/revanced/patches/tiktok/misc/settings/EnableOpenDebugPatch.kt
 *
 * TikTok 43.6.2: ReVanced MR !6535. OpenDebug hooks use heavy obfuscation; names differ by APK variant.
 * State class: many short LX/… UI states reuse obfuscated field names like LLILLL—selection is driven by the
 * unique Jetpack compose `LIZ` method that calls Context.getString(I) and reads LLILL (OpenDebug row).
 */

/*
 * Forked from:
 * https://gitlab.com/ReVanced/revanced-patches/-/blob/main/patches/src/main/kotlin/app/revanced/patches/tiktok/misc/settings/EnableOpenDebugPatch.kt
 */
package app.morphe.patches.tiktok.misc.settings

import app.morphe.patches.shared.compat.AppCompatibilities
import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import app.morphe.patcher.util.smali.ExternalLabel
import app.morphe.patches.tiktok.misc.extension.sharedExtensionPatch
import app.morphe.util.findMutableMethodOf
import app.morphe.util.getReference
import app.morphe.util.indexOfFirstInstructionOrThrow
import app.morphe.util.indexOfFirstInstructionReversedOrThrow
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method as SmaliMethod
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val SETTINGS_EXTENSION_CLASS_DESCRIPTOR = "Lapp/morphe/extension/tiktok/settings/TikTokActivityHook;"

private const val ANDROID_CONTEXT_GET_STRING = "Landroid/content/Context;->getString(I)Ljava/lang/String;"

private data class OpenDebugTargets(
    val stateClass: String,
    val ctorMutable: MutableMethod,
    val composeMutable: MutableMethod,
)

@Suppress("unused")
val enableOpenDebugPatch = bytecodePatch(
    name = "Enable Open Debug",
    description = "Re-enables the hidden \"Open debug\" entry. Supported on TikTok 43.6.2 only. (Not compatible with 43.8.3.)",
    default = true,
) {
    dependsOn(sharedExtensionPatch)

    compatibleWith(*AppCompatibilities.tiktok4362())

    execute {
        val initializeSettingsMethodDescriptor =
            "$SETTINGS_EXTENSION_CLASS_DESCRIPTOR->initialize(" +
                "Lcom/bytedance/ies/ugc/aweme/commercialize/compliance/personalization/AdPersonalizationActivity;" +
                ")Z"

        fun isOpenDebugRowCompose(method: SmaliMethod): Boolean {
            val impl = method.implementation ?: return false
            var hasGetString = false
            var hasLlill = false
            for (insn in impl.instructions) {
                when (insn.opcode) {
                    Opcode.INVOKE_VIRTUAL -> {
                        val ref = (insn as? ReferenceInstruction)?.reference as? MethodReference
                        if (ref?.toString() == ANDROID_CONTEXT_GET_STRING) hasGetString = true
                    }
                    Opcode.IGET_OBJECT -> {
                        val ref = (insn as? ReferenceInstruction)?.reference as? FieldReference
                        if (ref?.name == "LLILL") hasLlill = true
                    }
                    else -> {}
                }
            }
            return hasGetString && hasLlill
        }

        fun ctorWritesLlilllPriority(method: SmaliMethod, stateClassDef: ClassDef): Int? {
            val impl = method.implementation ?: return null
            val writes = impl.instructions.any { insn ->
                if (insn.opcode != Opcode.IPUT_OBJECT) return@any false
                val ref = (insn as? ReferenceInstruction)?.reference as? FieldReference ?: return@any false
                ref.name == "LLILLL" && ref.definingClass == stateClassDef.type
            }
            if (!writes) return null
            return when (method.name) {
                " " -> 3
                "<init>" -> 2
                else -> 1
            }
        }

        /**
         * One DEX pass: index class types, group OpenDebug-shaped `LIZ` composables by first param (state class).
         * LLILLL alone matches dozens of unrelated compose states; OpenDebug is the row that resolves title via
         * getString + LLILL (see patch below).
         */
        fun resolveOpenDebugTargets(): OpenDebugTargets {
            val composeByStateClass = linkedMapOf<String, MutableList<Pair<ClassDef, SmaliMethod>>>()
            val typeToClassDef = hashMapOf<String, ClassDef>()
            classDefForEach { classDef ->
                typeToClassDef[classDef.type] = classDef
                for (method in classDef.methods) {
                    if (method.name != "LIZ" || method.returnType != "V") continue
                    val p = method.parameterTypes
                    if (p.size != 5) continue
                    if (p[1] != "Z" || p[2] != "Z" || p[4] != "I") continue
                    if (!p[3].startsWith("LX/")) continue
                    val stateClass = p[0].toString()
                    composeByStateClass.getOrPut(stateClass) { mutableListOf() }.add(classDef to method)
                }
            }

            val resolved = mutableListOf<Pair<OpenDebugTargets, Int>>() // (target, bestCtorPriority)
            for ((stateClass, pairs) in composeByStateClass) {
                if (pairs.size != 1) continue
                val (composeClassDef, composeMethod) = pairs.single()
                if (!isOpenDebugRowCompose(composeMethod)) continue
                val stateClassDef = typeToClassDef[stateClass] ?: continue
                var bestCtor: SmaliMethod? = null
                var bestPri = -1
                for (m in stateClassDef.methods) {
                    val pr = ctorWritesLlilllPriority(m, stateClassDef) ?: continue
                    if (pr > bestPri) {
                        bestPri = pr
                        bestCtor = m
                    }
                }
                val ctor = bestCtor ?: continue
                val target = OpenDebugTargets(
                    stateClass = stateClass,
                    ctorMutable = mutableClassDefBy(stateClassDef).findMutableMethodOf(ctor),
                    composeMutable = mutableClassDefBy(composeClassDef).findMutableMethodOf(composeMethod),
                )
                resolved += target to bestPri
            }

            if (resolved.isEmpty()) {
                throw PatchException(
                    "Enable Open Debug: no OpenDebug row compose (LIZ + getString + LLILL) with unique state " +
                        "type and IPUT LLILLL. Use the 43.6.2 APK from ReVanced MR !6535.",
                )
            }
            // TikTok obfuscation can still produce multiple matching OpenDebug candidates.
            // Rank by the constructor priority we already computed per state class.
            return resolved.maxWith(
                compareBy<Pair<OpenDebugTargets, Int>> { it.second }
                    .thenBy { it.first.stateClass },
            ).first
        }

        val targets = resolveOpenDebugTargets()
        val openDebugStateCtorMutable = targets.ctorMutable
        val openDebugStateClass = targets.stateClass
        val composeMutable = targets.composeMutable

        fun clickLambdaReadsL1(method: SmaliMethod, classType: String): Boolean {
            val impl = method.implementation ?: return false
            return impl.instructions.any { insn ->
                if (insn.opcode != Opcode.IGET_OBJECT) return@any false
                val ref = (insn as? ReferenceInstruction)?.reference as? FieldReference ?: return@any false
                ref.name == "l1" && ref.definingClass == classType
            }
        }

        fun resolveClickWrapperMethod(): MutableMethod {
            OpenDebugCellClickWrapperFingerprint.methodOrNull?.let { return it }
            // Fallback: if the strict fingerprint is not unique in this exact APK build,
            // pick the best candidate deterministically by scoring its bytecode.
            val matches = mutableListOf<Triple<MutableMethod, Int, String>>() // (method, score, definingClass)
            classDefForEach { classDef ->
                if (!classDef.type.startsWith("Lkotlin/jvm/internal/AwS350")) return@classDefForEach
                for (method in classDef.methods) {
                    if (method.parameterTypes.size != 1 || method.parameterTypes[0] != classDef.type) continue
                    if (!method.name.matches(Regex("invoke\\\$\\d+"))) continue
                    if (!clickLambdaReadsL1(method, classDef.type)) continue
                    val score = run {
                        var s = 0
                        val impl = method.implementation ?: return@run 0
                        for (insn in impl.instructions) {
                            val refStr = (insn as? ReferenceInstruction)?.reference?.toString() ?: continue
                            if (refStr.contains("AdPersonalizationActivity")) s += 50
                            if (refStr.contains("Intent;->setAction")) s += 30
                            if (refStr.contains("Context;->startActivity")) s += 30
                            if (refStr.contains("revanced_settings")) s += 10
                        }
                        s
                    }
                    matches += Triple(mutableClassDefBy(classDef).findMutableMethodOf(method), score, classDef.type)
                }
            }
            val best = matches.maxWithOrNull(
                compareByDescending<Triple<MutableMethod, Int, String>> { it.second }
                    .thenBy { it.third },
            )
            if (best == null || best.second <= 0) {
                throw PatchException(
                    "Enable Open Debug: could not uniquely identify OpenDebug click handler. " +
                        "Strict fingerprint failed; bytecode scan did not find AdPersonalizationActivity wiring.",
                )
            }
            return best.first
        }

        SupportGroupDefaultStateFingerprint.method.apply {
            val aboutSgetIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.SGET_OBJECT && getReference<FieldReference>()?.name == "ABOUT"
            }

            val aboutAddInstruction = getInstruction<Instruction35c>(aboutSgetIndex + 1)
            val listRegister = aboutAddInstruction.registerC
            val itemRegister = aboutAddInstruction.registerD

            addInstructions(
                aboutSgetIndex + 2,
                """
                    sget-object v$itemRegister, LX/0mDW;->OPEN_DEBUG:LX/0mDW;
                    invoke-virtual { v$listRegister, v$itemRegister }, LX/165P;->add(Ljava/lang/Object;)Z
                """,
            )
        }

        AdPersonalizationActivityOnCreateFingerprint.method.apply {
            val initializeSettingsIndex = implementation!!.instructions.indexOfFirst { it.opcode == Opcode.INVOKE_SUPER } + 1
            val thisRegister = getInstruction<Instruction35c>(initializeSettingsIndex - 1).registerC
            val usableRegister = implementation!!.registerCount - parameters.size - 2

            addInstructionsWithLabels(
                initializeSettingsIndex,
                """
                    invoke-static {v$thisRegister}, $initializeSettingsMethodDescriptor
                    move-result v$usableRegister
                    if-eqz v$usableRegister, :do_not_open
                    return-void
                """,
                ExternalLabel("do_not_open", getInstruction(initializeSettingsIndex)),
            )
        }

        openDebugStateCtorMutable.apply {
            val titleValuePutIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.IPUT_OBJECT && getReference<FieldReference>()?.name == "LLILLL"
            }

            val valueRegister = getInstruction<TwoRegisterInstruction>(titleValuePutIndex).registerA
            addInstruction(titleValuePutIndex, "const-string v$valueRegister, \"ReVanced settings\"")
        }

        val compose: SmaliMethod = composeMutable
        val getStringInvokeIndex = compose.indexOfFirstInstructionOrThrow {
            opcode == Opcode.INVOKE_VIRTUAL &&
                getReference<MethodReference>()?.toString() == ANDROID_CONTEXT_GET_STRING
        }
        val afterTitleIndex = getStringInvokeIndex + 2
        val moveResultIndex = getStringInvokeIndex + 1
        val titleStringRegister = compose.getInstruction<OneRegisterInstruction>(moveResultIndex).registerA
        val titleIdFieldGetIndex = compose.indexOfFirstInstructionReversedOrThrow(getStringInvokeIndex) {
            opcode == Opcode.IGET_OBJECT && getReference<FieldReference>()?.name == "LLILL"
        }
        val stateRegister = compose.getInstruction<TwoRegisterInstruction>(titleIdFieldGetIndex).registerB

        composeMutable.addInstructionsWithLabels(
            getStringInvokeIndex,
            """
                iget-object v$titleStringRegister, v$stateRegister, $openDebugStateClass->LLILLL:Ljava/lang/String;
                if-nez v$titleStringRegister, :revanced_title_done
            """,
            ExternalLabel("revanced_title_done", compose.getInstruction(afterTitleIndex)),
        )

        OpenDebugCellVmDefaultStateFingerprint.methodOrNull?.apply {
            val iconIdLiteralIndex = indexOfFirstInstructionOrThrow {
                this is NarrowLiteralInstruction && narrowLiteral == 0x7f0107e3
            }

            val iconRegister = getInstruction<OneRegisterInstruction>(iconIdLiteralIndex).registerA
            replaceInstruction(iconIdLiteralIndex, "const v$iconRegister, 0x7f010088")
        }

        val clickWrapperMethod = resolveClickWrapperMethod()
        val openDebugClickWrapperClass = clickWrapperMethod.definingClass
        clickWrapperMethod.apply {
            addInstructions(
                0,
                """
                    iget-object v0, p0, $openDebugClickWrapperClass->l1:Ljava/lang/Object;
                    check-cast v0, Landroid/content/Context;
                    new-instance v1, Landroid/content/Intent;
                    const-class v2, Lcom/bytedance/ies/ugc/aweme/commercialize/compliance/personalization/AdPersonalizationActivity;
                    invoke-direct {v1, v0, v2}, Landroid/content/Intent;-><init>(Landroid/content/Context;Ljava/lang/Class;)V
                    const-string v2, "revanced_settings"
                    invoke-virtual { v1, v2 }, Landroid/content/Intent;->setAction(Ljava/lang/String;)Landroid/content/Intent;
                    const/high16 v2, 0x10000000
                    invoke-virtual { v1, v2 }, Landroid/content/Intent;->addFlags(I)Landroid/content/Intent;
                    invoke-virtual { v0, v1 }, Landroid/content/Context;->startActivity(Landroid/content/Intent;)V
                    sget-object v0, Lkotlin/Unit;->LIZ:Lkotlin/Unit;
                    return-object v0
                """,
            )
        }
    }
}
