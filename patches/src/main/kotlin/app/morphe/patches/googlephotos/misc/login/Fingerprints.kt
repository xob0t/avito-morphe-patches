/*
 * Forked from:
 * https://gitlab.com/ReVanced/revanced-patches/-/blob/main/patches/src/main/kotlin/app/revanced/patches/googlephotos/misc/login/Fingerprints.kt
 */
package app.morphe.patches.googlephotos.misc.login

import app.morphe.patcher.Fingerprint
import app.morphe.util.getReference
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

/**
 * Matches `aext.d()` — the AccountValidityMonitor method that enqueues `CheckAccountTask`
 * and (under MicroG) clears the selected account on resume.
 */
internal object AccountValidityMonitorCheckFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf(),
    custom = { method, _ ->
        method.implementation?.instructions?.let { insns ->
            insns.any {
                it.getReference<FieldReference>()?.let { ref ->
                    ref.definingClass == "Laexv;" && ref.name == "a" && ref.type == "I"
                } == true
            } && insns.any {
                it.getReference<MethodReference>()?.toString()?.contains(
                    "AccountValidityMonitor\$CheckAccountTask"
                ) == true
            }
        } ?: false
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
    custom = { method, _ ->
        method.implementation?.instructions?.let { insns ->
            insns.any {
                it.getReference<MethodReference>()?.toString() == "Lasgb;->b()Z"
            } && insns.any {
                it.getReference<MethodReference>()?.toString() == "Laexs;->o(I)V"
            }
        } ?: false
    },
)

