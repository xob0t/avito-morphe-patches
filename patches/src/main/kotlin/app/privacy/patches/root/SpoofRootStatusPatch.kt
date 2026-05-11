package app.privacy.patches.root

import app.morphe.patcher.extensions.InstructionExtensions.instructionsOrNull
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.RegisterRangeInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference

private val SU_BINARY_PATHS = setOf(
    "/cache/",
    "/data/",
    "/data/local/",
    "/data/local/bin/",
    "/data/local/xbin/",
    "/dev/",
    "/sbin/",
    "/su/bin/",
    "/system/bin/",
    "/system/bin/.ext/",
    "/system/bin/failsafe/",
    "/system/sd/xbin/",
    "/system/usr/we-need-root/",
    "/system/xbin/",
)

private val SU_BINARY_NAMES = setOf("su", "busybox", "magisk")

private val SUPERUSER_FILES = setOf(
    "/dev/com.koushikdutta.superuser.daemon",
    "/dev/socket/magiskd",
    "/dev/socket/su",
    "/sbin/su",
    "/system/app/Magisk.apk",
    "/system/app/SuperSU.apk",
    "/system/app/Superuser.apk",
    "/system/bin/.ext/su",
    "/system/bin/su",
    "/system/etc/init.d/99SuperSUDaemon",
    "/system/priv-app/Magisk.apk",
    "/system/usr/we-need-root/su",
    "/system/xbin/daemonsu",
    "/system/xbin/su",
)

private val ROOT_PACKAGES = setOf(
    "com.alephzain.framaroot",
    "com.kingo.root",
    "com.kingroot.kinguser",
    "com.koushikdutta.superuser",
    "com.noshufou.android.su",
    "com.noshufou.android.su.elite",
    "com.smedialink.oneclickroot",
    "com.thirdparty.superuser",
    "com.topjohnwu.magisk",
    "com.yellowes.su",
    "com.zhiqupk.root.global",
    "eu.chainfire.supersu",
)

private val ROOT_CLOAKING_PACKAGES = setOf(
    "com.amphoras.hidemyroot",
    "com.amphoras.hidemyrootadfree",
    "com.devadvance.rootcloak",
    "com.devadvance.rootcloakplus",
    "com.formyhm.hideroot",
    "com.formyhm.hiderootPremium",
    "com.saurik.substrate",
    "com.solohsu.android.edxp.manager",
    "com.zachspong.temprootremovejb",
    "de.robv.android.xposed.installer",
    "org.meowcat.edxposed.manager",
)

private val DANGEROUS_PACKAGES = setOf(
    "catch_.me_.if_.you_.can_",
    "cc.madkite.freedom",
    "com.allinone.free",
    "com.android.camera.update",
    "com.android.vending.billing.InAppBillingService.COIN",
    "com.android.vending.billing.InAppBillingService.LUCK",
    "com.android.wp.net.log",
    "com.baseappfull.fwd",
    "com.blackmartalpha",
    "com.charles.lpoqasert",
    "com.chelpus.lackypatch",
    "com.chelpus.luckypatcher",
    "com.cih.game_cih",
    "com.dimonvideo.luckypatcher",
    "com.dv.marketmod.installer",
    "com.koushikdutta.rommanager",
    "com.koushikdutta.rommanager.license",
    "com.ramdroid.appquarantine",
    "com.ramdroid.appquarantinepro",
    "com.repodroid.app",
    "com.xmodgame",
    "com.zmapp",
    "org.blackmart.market",
    "org.creeplays.hack",
    "org.mobilism.android",
)

private val ROOT_INDICATOR_STRINGS = buildSet {
    addAll(SUPERUSER_FILES)
    addAll(ROOT_PACKAGES)
    addAll(ROOT_CLOAKING_PACKAGES)
    addAll(DANGEROUS_PACKAGES)
    addAll(SU_BINARY_NAMES)
    add("magisk")
    add("superuser")
    add("supersu")
    add("test-keys")
}

private val ROOT_BUILD_PROPS = setOf(
    "ro.build.tags",
    "ro.build.type",
    "ro.debuggable",
    "ro.secure",
)

private val SAFE_SYSTEM_PROPERTY_VALUES = mapOf(
    "ro.build.tags" to "release-keys",
    "ro.build.type" to "user",
    "ro.debuggable" to "0",
    "ro.secure" to "1",
)

private fun Instruction.methodReferenceOrNull(): MethodReference? =
    (this as? ReferenceInstruction)?.reference as? MethodReference

private fun Instruction.fieldReferenceOrNull(): FieldReference? =
    (this as? ReferenceInstruction)?.reference as? FieldReference

private fun Instruction.stringReferenceOrNull(): String? =
    ((this as? ReferenceInstruction)?.reference as? StringReference)?.string

private fun Instruction.registers(): List<Int> =
    when (this) {
        is FiveRegisterInstruction -> listOf(registerC, registerD, registerE, registerF, registerG)
            .take(registerCount)

        is RegisterRangeInstruction -> (startRegister until startRegister + registerCount).toList()
        else -> emptyList()
    }

private fun Instruction.writesRegister(register: Int): Boolean {
    if (this !is OneRegisterInstruction || registerA != register) return false

    return when (opcode) {
        Opcode.CONST,
        Opcode.CONST_4,
        Opcode.CONST_16,
        Opcode.CONST_CLASS,
        Opcode.CONST_HIGH16,
        Opcode.CONST_STRING,
        Opcode.CONST_STRING_JUMBO,
        Opcode.MOVE,
        Opcode.MOVE_16,
        Opcode.MOVE_EXCEPTION,
        Opcode.MOVE_FROM16,
        Opcode.MOVE_OBJECT,
        Opcode.MOVE_OBJECT_16,
        Opcode.MOVE_OBJECT_FROM16,
        Opcode.MOVE_RESULT,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.NEW_INSTANCE,
        Opcode.SGET,
        Opcode.SGET_BOOLEAN,
        Opcode.SGET_BYTE,
        Opcode.SGET_CHAR,
        Opcode.SGET_OBJECT,
        Opcode.SGET_SHORT,
            -> true

        else -> false
    }
}

private fun List<Instruction>.constantStringForRegisterBefore(index: Int, register: Int): String? {
    for (candidateIndex in index - 1 downTo maxOf(0, index - 16)) {
        val candidate = this[candidateIndex]
        if (
            candidate is OneRegisterInstruction &&
            candidate.registerA == register &&
            candidate.opcode in setOf(Opcode.CONST_STRING, Opcode.CONST_STRING_JUMBO)
        ) {
            return candidate.stringReferenceOrNull()
        }

        if (candidate.writesRegister(register)) return null
    }

    return null
}

private fun String.isRootIndicatorPathOrName(): Boolean =
    this in ROOT_INDICATOR_STRINGS ||
        SU_BINARY_PATHS.any { path -> this == path || this.startsWith(path) } ||
        SU_BINARY_NAMES.any { name -> this == name || this.endsWith("/$name") }

private fun Method.hasRootDetectionContext(): Boolean {
    val instructions = instructionsOrNull?.toList() ?: return false

    return instructions.any { instruction ->
        val string = instruction.stringReferenceOrNull() ?: return@any false
        string.isRootIndicatorPathOrName() || string in ROOT_BUILD_PROPS
    }
}

private fun MethodReference.isFileExists() =
    definingClass == "Ljava/io/File;" &&
        name == "exists" &&
        parameterTypes.isEmpty() &&
        returnType == "Z"

private fun MethodReference.isFileCanRead() =
    definingClass == "Ljava/io/File;" &&
        name == "canRead" &&
        parameterTypes.isEmpty() &&
        returnType == "Z"

private fun MethodReference.isRuntimeExec() =
    definingClass == "Ljava/lang/Runtime;" &&
        name == "exec" &&
        returnType == "Ljava/lang/Process;"

private fun MethodReference.isProcessBuilderStart() =
    definingClass == "Ljava/lang/ProcessBuilder;" &&
        name == "start" &&
        parameterTypes.isEmpty() &&
        returnType == "Ljava/lang/Process;"

private fun MethodReference.isPackageManagerGetPackageInfo() =
    definingClass == "Landroid/content/pm/PackageManager;" &&
        name == "getPackageInfo" &&
        returnType == "Landroid/content/pm/PackageInfo;"

private fun MethodReference.isPackageManagerGetApplicationInfo() =
    definingClass == "Landroid/content/pm/PackageManager;" &&
        name == "getApplicationInfo" &&
        returnType == "Landroid/content/pm/ApplicationInfo;"

private fun MethodReference.isSystemGetProperty() =
    (definingClass == "Ljava/lang/System;" && name == "getProperty") ||
        (definingClass == "Landroid/os/SystemProperties;" && name == "get")

private fun FieldReference.isBuildTags() =
    definingClass == "Landroid/os/Build;" &&
        name == "TAGS" &&
        type == "Ljava/lang/String;"

@Suppress("unused")
val spoofRootStatusPatch = bytecodePatch(
    name = "Spoof root status",
    description = "Spoofs root state through common file, package, command, and build property checks.",
    default = false,
) {
    execute {
        var patchedFileChecks = 0
        var patchedCommandExecutions = 0
        var patchedPackageQueries = 0
        var patchedBuildTags = 0
        var patchedSystemProperties = 0

        classDefForEach { classDef ->
            if (classDef.methods.none { it.hasRootDetectionContext() }) return@classDefForEach

            mutableClassDefBy(classDef).methods.forEach { method ->
                if (!method.hasRootDetectionContext()) return@forEach

                val instructions = method.instructionsOrNull ?: return@forEach
                val instructionList = instructions.toList()

                instructionList.forEachIndexed { index, instruction ->
                    when {
                        instruction.opcode in setOf(Opcode.INVOKE_VIRTUAL, Opcode.INVOKE_VIRTUAL_RANGE) -> {
                            val reference = instruction.methodReferenceOrNull() ?: return@forEachIndexed

                            when {
                                reference.isFileExists() || reference.isFileCanRead() -> {
                                    val moveResult = instructionList.getOrNull(index + 1) as? OneRegisterInstruction
                                        ?: return@forEachIndexed
                                    if (moveResult.opcode != Opcode.MOVE_RESULT) return@forEachIndexed

                                    method.replaceInstruction(index + 1, "const/4 v${moveResult.registerA}, 0x0")
                                    patchedFileChecks++
                                }

                                reference.isRuntimeExec() || reference.isProcessBuilderStart() -> {
                                    val moveResult = instructionList.getOrNull(index + 1) as? OneRegisterInstruction
                                        ?: return@forEachIndexed
                                    if (moveResult.opcode != Opcode.MOVE_RESULT_OBJECT) return@forEachIndexed

                                    method.replaceInstruction(index + 1, "const/4 v${moveResult.registerA}, 0x0")
                                    patchedCommandExecutions++
                                }

                                reference.isPackageManagerGetPackageInfo() ||
                                    reference.isPackageManagerGetApplicationInfo() -> {
                                    val packageRegister = instruction.registers().getOrNull(1)
                                        ?: return@forEachIndexed
                                    val packageName =
                                        instructionList.constantStringForRegisterBefore(index, packageRegister)
                                            ?: return@forEachIndexed
                                    if (
                                        packageName !in ROOT_PACKAGES &&
                                        packageName !in ROOT_CLOAKING_PACKAGES &&
                                        packageName !in DANGEROUS_PACKAGES
                                    ) {
                                        return@forEachIndexed
                                    }

                                    val moveResult = instructionList.getOrNull(index + 1) as? OneRegisterInstruction
                                        ?: return@forEachIndexed
                                    if (moveResult.opcode != Opcode.MOVE_RESULT_OBJECT) return@forEachIndexed

                                    method.replaceInstruction(index + 1, "const/4 v${moveResult.registerA}, 0x0")
                                    patchedPackageQueries++
                                }

                                reference.isSystemGetProperty() -> {
                                    val propertyRegister = instruction.registers().firstOrNull()
                                        ?: return@forEachIndexed
                                    val propertyName =
                                        instructionList.constantStringForRegisterBefore(index, propertyRegister)
                                            ?: return@forEachIndexed
                                    val safeValue = SAFE_SYSTEM_PROPERTY_VALUES[propertyName]
                                        ?: return@forEachIndexed

                                    val moveResult = instructionList.getOrNull(index + 1) as? OneRegisterInstruction
                                        ?: return@forEachIndexed
                                    if (moveResult.opcode != Opcode.MOVE_RESULT_OBJECT) return@forEachIndexed

                                    method.replaceInstruction(
                                        index + 1,
                                        "const-string v${moveResult.registerA}, \"$safeValue\"",
                                    )
                                    patchedSystemProperties++
                                }
                            }
                        }

                        instruction.opcode == Opcode.SGET_OBJECT -> {
                            val field = instruction.fieldReferenceOrNull() ?: return@forEachIndexed
                            if (!field.isBuildTags()) return@forEachIndexed

                            val destinationRegister = (instruction as OneRegisterInstruction).registerA
                            method.replaceInstruction(
                                index,
                                "const-string v$destinationRegister, \"release-keys\"",
                            )
                            patchedBuildTags++
                        }
                    }
                }
            }
        }

        if (
            patchedFileChecks == 0 &&
            patchedCommandExecutions == 0 &&
            patchedPackageQueries == 0 &&
            patchedBuildTags == 0 &&
            patchedSystemProperties == 0
        ) {
            throw PatchException("No root status call sites were found")
        }

        println(
            "Spoof root status: patched $patchedFileChecks file checks, " +
                "$patchedCommandExecutions command executions, " +
                "$patchedPackageQueries package queries, " +
                "$patchedBuildTags Build.TAGS reads, and " +
                "$patchedSystemProperties system property reads.",
        )
    }
}
