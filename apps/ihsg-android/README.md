# IHSG Stock Analysis Android

Native Android client for the IHSG-focused Daily Stock Analysis backend.

## Release Build

The app expects the FastAPI backend to be reachable from the phone or emulator:

- Emulator default: `http://10.0.2.2:8000`
- Physical device: use the computer LAN address, for example `http://192.168.1.20:8000`

Build commands:

```powershell
$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat testDebugUnitTest assembleRelease bundleRelease
```

Release outputs:

- APK: `app/build/outputs/apk/release/app-release.apk`
- AAB: `app/build/outputs/bundle/release/app-release.aab`

The app only accepts IDX/Yahoo Jakarta symbols with `.JK`, such as `BBCA.JK`, `TLKM.JK`, and `ASII.JK`.
