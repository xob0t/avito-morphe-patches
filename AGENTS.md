# Agent Notes

## Local Build

This repo uses the Morphe Gradle plugin from GitHub Packages. Local Gradle runs need GitHub credentials and an Android SDK path in the current shell.

PowerShell setup:

```powershell
$env:GITHUB_ACTOR = 'xob0t'
$env:GITHUB_TOKEN = gh auth token
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
```

Build the patch bundle and regenerate patch metadata:

```powershell
.\gradlew :patches:buildAndroid generatePatchesList
```

If Gradle fails while applying `app.morphe.patches`, check `GITHUB_ACTOR` and `GITHUB_TOKEN`. If it fails with `SDK location not found`, set `ANDROID_HOME`.

## Stale Artifact Gotcha

`generatePatchesList` reads an `.mpp` from `patches/build/libs`. If old `.mpp` files are present, it can pick the wrong bundle and write an incorrect top-level version to `patches-list.json`.

Before regenerating metadata after a version change, remove stale artifacts:

```powershell
Remove-Item -LiteralPath patches\build\libs\patches-1.1.2*.mpp -ErrorAction SilentlyContinue
.\gradlew generatePatchesList
```

After generation, verify:

```powershell
(Get-Content -Raw patches-list.json | ConvertFrom-Json).version
Get-ChildItem patches\build\libs | Select-Object Name,Length
```

For checked-in metadata, keep `patches-list.json` version consistent with `patches-bundle.json` and the release version format already used in the repo.

## CLI Patch Testing

Use the existing CLI jar:

```powershell
java -jar C:\Users\admin\Documents\New project 4\tools\morphe-cli.jar --help
```

Test one patch in isolation with exclusive mode:

```powershell
$mpp = 'C:\Users\admin\Documents\New project 4\morphe-patches-template\patches\build\libs\patches-1.2.0-dev.1.mpp'
$outDir = 'C:\Users\admin\Documents\New project 4\avito-patched\fingerprint-test'
New-Item -ItemType Directory -Force -Path $outDir | Out-Null

java -jar 'C:\Users\admin\Documents\New project 4\tools\morphe-cli.jar' patch `
  --exclusive `
  --enable='Disable update prompts' `
  --patches="$mpp" `
  --out="$outDir\avito-224.1-update-prompt-test.apk" `
  --result-file="$outDir\avito-224.1-result.json" `
  --temporary-files-path="$outDir\tmp-224.1" `
  --unsigned `
  'C:\Users\admin\Documents\New project 4\avito_224.1.apk'
```

Useful local Avito samples:

```text
C:\Users\admin\Documents\New project 4\avito_221.0.apk
C:\Users\admin\Documents\New project 4\avito_224.1.apk
C:\Users\admin\Documents\New project 4\avito.apk
```

## Release Workflow

CI should produce release artifacts. Avoid manually uploading rebuilt `.mpp` files unless fixing a broken release.

The release workflow builds with:

```bash
./gradlew :patches:buildAndroid generatePatchesList clean
```

`dev` publishes pre-release builds. `main` publishes stable builds.
