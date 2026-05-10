/*
 * Forked from:
 * https://gitlab.com/ReVanced/revanced-patches/-/blob/main/patches/src/main/kotlin/app/revanced/patches/googlephotos/misc/login/Fingerprints.kt
 */
package app.morphe.patches.googlephotos.misc.login

import app.morphe.patcher.Fingerprint
import app.morphe.util.getReference
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference

private fun Method.referencesString(value: String) =
    implementation?.instructions?.any {
        it.getReference<StringReference>()?.string == value
    } == true

private fun Method.referencesStringContaining(value: String) =
    implementation?.instructions?.any {
        it.getReference<StringReference>()?.string?.contains(value) == true
    } == true

private fun Method.referencesMethod(returnType: String, parameters: List<String>? = null) =
    implementation?.instructions?.any {
        it.getReference<MethodReference>()?.let { ref ->
            ref.returnType == returnType && (parameters == null || ref.parameterTypes.toList() == parameters)
        } == true
    } == true

private fun Method.referencesVoidMethodWithSingleObjectParameter() =
    implementation?.instructions?.any {
        it.getReference<MethodReference>()?.let { ref ->
            ref.returnType == "V" &&
                ref.parameterTypes.size == 1 &&
                ref.parameterTypes.first().startsWith("L")
        } == true
    } == true

private fun Method.referencesIntLiteral(value: Int) =
    implementation?.instructions?.any {
        it is NarrowLiteralInstruction && it.narrowLiteral == value
    } == true

private fun ClassDef.hasMethodReferencingString(value: String) =
    methods.any { it.referencesString(value) }

private fun ClassDef.hasMethodReferencingStringContaining(value: String) =
    methods.any { it.referencesStringContaining(value) }

/**
 * Matches `aext.d()` — the AccountValidityMonitor method that enqueues `CheckAccountTask`
 * and (under MicroG) clears the selected account on resume.
 */
internal object AccountValidityMonitorCheckFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf(),
    custom = { method, classDef ->
        classDef.hasMethodReferencingString("AccountValidityMonitor") &&
            classDef.hasMethodReferencingString("com.google.android.apps.photos.login.AccountValidityMonitor.CheckAccountTask") &&
            method.implementation?.instructions?.let { instructions ->
                instructions.any {
                it.getReference<FieldReference>()?.let { ref ->
                    ref.name == "a" && ref.type == "I"
                } == true
            } && method.referencesVoidMethodWithSingleObjectParameter()
        } == true
    },
)

/**
 * Matches `aeye.f()` — the frictionless-login eligibility check.
 *
 * Under MicroG this can return false and clear the selected account.
 */
internal object FrictionlessEligibilityFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf(),
    custom = { method, classDef ->
        classDef.hasMethodReferencingStringContaining("maybeStartFrictionless") &&
            classDef.hasMethodReferencingString("ProvideFrctAccountTask") &&
            method.referencesMethod("Z", emptyList()) &&
            method.referencesMethod("V", listOf("I")) &&
            method.referencesIntLiteral(-1)
    },
)

