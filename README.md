# xob0t Morphe Patches

Personal Morphe patches for Android apps.

## Patches

<!-- PATCHES_START EXPANDED -->
> **[v1.1.1](https://github.com/xob0t/xob0t-morphe-patches/releases/tag/v1.1.1)**&nbsp;&nbsp;•&nbsp;&nbsp;`main`&nbsp;&nbsp;•&nbsp;&nbsp;14 patches total
<details open>
<summary>📦 Avito&nbsp;&nbsp;•&nbsp;&nbsp;4 patches</summary>
<br>

**🎯 Supported versions:**

| all |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Avito privacy](#avito-privacy) | Disables Avito first-party clickstream analytics and Avito's direct Adjust telemetry wrapper. |  |
| [Disable update prompts](#disable-update-prompts) | Prevents Avito's force-update screen opener from launching update screens. |  |
| [Hide Avi bottom tab](#hide-avi-bottom-tab) | Removes the Avi assistant button from Avito's bottom navigation bar. |  |
| [Remove ads](#remove-ads) | Disables Avito ads by removing ad SDK entry points and short-circuiting commercial banner loading. |  |

</details>

<details open>
<summary>🌐 Universal&nbsp;&nbsp;•&nbsp;&nbsp;10 patches</summary>
<br>

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Disable Adjust](#disable-adjust) | Disables Adjust attribution manifest entry points. |  |
| [Disable AppMetrica](#disable-appmetrica) | Disables AppMetrica and legacy Yandex Metrica SDK entry points. |  |
| [Disable AppsFlyer](#disable-appsflyer) | Disables AppsFlyer install referrer and attribution manifest entry points. |  |
| [Disable Firebase telemetry](#disable-firebase-telemetry) | Disables Firebase telemetry collection flags and DataTransport sender entry points. |  |
| [Disable Google Analytics](#disable-google-analytics) | Disables legacy Google Analytics manifest entry points. |  |
| [Disable MyTracker](#disable-mytracker) | Disables MyTracker manifest entry points. |  |
| [Disable RuStore metrics](#disable-rustore-metrics) | Disables RuStore metrics manifest entry points. |  |
| [Disable Sentry telemetry](#disable-sentry-telemetry) | Disables Sentry telemetry by turning off SDK auto-init and clearing the DSN. |  |
| [Hide USB debugging](#hide-usb-debugging) | Prevents apps from detecting USB debugging and related developer settings through common Android APIs. |  |
| [Hide VPN detection](#hide-vpn-detection) | Prevents apps from detecting VPN state through common Android network APIs. |  |

</details>

<!-- PATCHES_END -->

## Usage

Add this repository as a remote patch source in Morphe:

```text
https://github.com/xob0t/xob0t-morphe-patches
```

Or use the raw bundle metadata URL:

```text
https://raw.githubusercontent.com/xob0t/xob0t-morphe-patches/main/patches-bundle.json
```

## License

GPLv3. See [LICENSE](LICENSE).
