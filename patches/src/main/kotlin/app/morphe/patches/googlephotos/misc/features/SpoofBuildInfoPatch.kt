/*
 * Forked from:
 * https://github.com/ReVanced/revanced-patches/blob/6b06b9d1328b971a06d10b4247f4c10f050e4f61/patches/src/main/kotlin/app/revanced/patches/googlephotos/misc/features/SpoofBuildInfoPatch.kt
 */

package app.morphe.patches.googlephotos.misc.features

import app.morphe.patcher.patch.bytecodePatch

// Placeholder patch: build spoofing is currently handled elsewhere.
// This keeps the dependency from SpoofFeaturesPatch satisfied without
// requiring the shared all.misc.build helpers from the Morphe template.
val spoofBuildInfoPatch = bytecodePatch(
    description = "No-op build spoof placeholder for Google Photos.",
) {
    execute {
        // Intentionally left blank.
    }
}

