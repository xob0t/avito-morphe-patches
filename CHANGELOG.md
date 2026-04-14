## [1.0.4](https://github.com/RookieEnough/De-ReVanced/compare/v1.0.3...v1.0.4) (2026-04-14)

### Bug Fixes

* **Google Photos:** fix “Select account” prompt on every restart under MicroG by preventing internal account-selection validation from clearing the selected account (tested on **7.66.00**).
* **Google Recorder:** improve `Remove device restrictions` fingerprint reliability with `custom` matching plus a bytecode-content fallback when class names move.

### TikTok

* Enable **all** TikTok patches by default on **43.6.2** and **43.8.3**.
* Keep **Settings** + **Enable Open Debug** as **43.6.2-only** (not compatible with 43.8.3).

## [1.0.3](https://github.com/RookieEnough/De-ReVanced/compare/v1.0.2...v1.0.3) (2026-04-02)


### Bug Fixes

* release v1.0.3 with Google Recorder fix ([c03c701](https://github.com/RookieEnough/De-ReVanced/commit/c03c701a6ba658afb4f4f2093faca9e6bc91e49c))





### Bug Fixes

* **Google Recorder — Remove device restrictions:** fingerprint and bytecode hook updated for current app builds — match `RecorderApplication.onCreate` without a fixed feature string, detect any `com.google.android.feature…PIXEL…` `const-string`, and apply the restriction bypass (fixes fingerprint mismatch on newer Recorder versions).

### Maintenance

* **Attribution:** add a standard `Forked from` source header across Kotlin sources that were still missing it.

## [1.0.2](https://github.com/RookieEnough/De-ReVanced/compare/v1.0.1...v1.0.2) (2026-04-02)


### Bug Fixes

* **ci:** restore semantic-release rules so feat, fix, and release commits trigger versions ([3728b97](https://github.com/RookieEnough/De-ReVanced/commit/3728b978519854968a56847075aa9d58565a85e3))
* release v1.0.3 with Google Recorder fix ([c03c701](https://github.com/RookieEnough/De-ReVanced/commit/c03c701a6ba658afb4f4f2093faca9e6bc91e49c))





### Bug Fixes

* **ci:** restore semantic-release rules so feat, fix, and release commits trigger versions ([3728b97](https://github.com/RookieEnough/De-ReVanced/commit/3728b978519854968a56847075aa9d58565a85e3))
* **ci:** append `CHANGELOG.md` section to GitHub release notes when present

### Morphe Manager integration

* Bumped Morphe Gradle plugin to **1.2.0** and **morphe-patcher** to **1.3.3** (GitHub Packages: `gpr.user` / `gpr.key`).
* All patches declare **`compatibleWith(Compatibility(...))`** via `AppCompatibilities` so Manager shows human-readable app titles and icon tint colors.
* **`patches-list.json`** now includes a root **`appNames`** map (package → display name) and, for each patch, a **`compatibility`** array with `name`, `packageName`, `appIconColor`, optional `description`, and **`targets`** (version pins). This fixes the detail view where only package IDs appeared after tapping an app.
* **Bundle artwork:** place **`patch-bundle.png`** in the same directory as **`patch-bundles.json`** so Morphe Manager can show that image instead of the user avatar for the bundle (per upstream guidance).

### Compatibility tuning

* **Google News** (`com.google.android.apps.magazines`): pinned to version **5.108.0.644447823** for all News-related patches.
* **TikTok:** **Settings** and **Enable Open Debug** remain **43.6.2-only** (not supported on 43.8.3); descriptions state this explicitly. Other TikTok patches keep their existing 43.6.2 / 43.8.3 splits.

### New app targets (16)

Patches and extensions were added or expanded for:

1. Bandcamp  
2. Angulus  
3. GMX Mail  
4. Free Phone (GMX)  
5. Hex Editor  
6. Google Recorder  
7. irplus  
8. NU.nl  
9. Nothing X  
10. Peacock TV  
11. Viber  
12. TikTok  
13. Twitch  
14. Adobe Photoshop Mix  
15. Threads  
16. Facebook  

### Notes

* Regenerate **`patches-list.json`** with `./gradlew :patches:generatePatchesList` (or full `:patches:build`) after changes.

## [1.0.3](https://github.com/RookieEnough/De-ReVanced/compare/v1.0.2...v1.0.3) (2026-04-01)

### Bug Fixes

* **Google Recorder — Remove device restrictions:** fingerprint and bytecode hook updated for current app builds — match `RecorderApplication.onCreate` without a fixed feature string, detect any `com.google.android.feature…PIXEL…` `const-string`, and apply the restriction bypass (fixes fingerprint mismatch on newer Recorder versions).

### Maintenance

* **Attribution:** add a standard `Forked from` source header across Kotlin sources that were still missing it.

## [1.0.2](https://github.com/RookieEnough/De-ReVanced/compare/v1.0.1...v1.0.2) (2026-04-01)

### Bug Fixes

* **ci:** restore semantic-release rules so feat, fix, and release commits trigger versions ([3728b97](https://github.com/RookieEnough/De-ReVanced/commit/3728b978519854968a56847075aa9d58565a85e3))
* **ci:** append `CHANGELOG.md` section to GitHub release notes when present

### Morphe Manager integration

* Bumped Morphe Gradle plugin to **1.2.0** and **morphe-patcher** to **1.3.3** (GitHub Packages: `gpr.user` / `gpr.key`).
* All patches declare **`compatibleWith(Compatibility(...))`** via `AppCompatibilities` so Manager shows human-readable app titles and icon tint colors.
* **`patches-list.json`** now includes a root **`appNames`** map (package → display name) and, for each patch, a **`compatibility`** array with `name`, `packageName`, `appIconColor`, optional `description`, and **`targets`** (version pins). This fixes the detail view where only package IDs appeared after tapping an app.
* **Bundle artwork:** place **`patch-bundle.png`** in the same directory as **`patch-bundles.json`** so Morphe Manager can show that image instead of the user avatar for the bundle (per upstream guidance).

### Compatibility tuning

* **Google News** (`com.google.android.apps.magazines`): pinned to version **5.108.0.644447823** for all News-related patches.
* **TikTok:** **Settings** and **Enable Open Debug** remain **43.6.2-only** (not supported on 43.8.3); descriptions state this explicitly. Other TikTok patches keep their existing 43.6.2 / 43.8.3 splits.

### New app targets (16)

Patches and extensions were added or expanded for:

1. Bandcamp  
2. Angulus  
3. GMX Mail  
4. Free Phone (GMX)  
5. Hex Editor  
6. Google Recorder  
7. irplus  
8. NU.nl  
9. Nothing X  
10. Peacock TV  
11. Viber  
12. TikTok  
13. Twitch  
14. Adobe Photoshop Mix  
15. Threads  
16. Facebook  

### Notes

* Regenerate **`patches-list.json`** with `./gradlew :patches:generatePatchesList` (or full `:patches:build`) after changes.

---

## [1.0.1](https://github.com/RookieEnough/De-ReVanced/compare/v1.0.0...v1.0.1) (2026-03-06)

### Bug Fixes

* Fixed all issues related to Google Photos  ([58f8bc8](https://github.com/RookieEnough/De-ReVanced/commit/58f8bc80dfd601cfe5a5dfee81e64d78bab0f496))

### Features

* Added Messenger patches — disable typing indicator, hide inbox ads, hide inbox subtabs, hide Facebook button, remove Meta AI ([58f8bc8](https://github.com/RookieEnough/De-ReVanced/commit/58f8bc80dfd601cfe5a5dfee81e64d78bab0f496))
* Added Inshorts patches — hide ads ([58f8bc8](https://github.com/RookieEnough/De-ReVanced/commit/58f8bc80dfd601cfe5a5dfee81e64d78bab0f496))
* Added Amazon Shopping patches — always allow deep-linking ([58f8bc8](https://github.com/RookieEnough/De-ReVanced/commit/58f8bc80dfd601cfe5a5dfee81e64d78bab0f496))
