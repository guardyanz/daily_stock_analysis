# IHSG Stock Analysis Android

Native Android client for the IHSG-focused Daily Stock Analysis backend.

## Release Build

The app expects the FastAPI backend to be reachable from the phone or emulator:

- Emulator default: `http://10.0.2.2:8000`
- Physical device: use the computer LAN address, for example `http://192.168.1.20:8000`

Build commands:

Use JDK 17 and Android SDK 36. Set `JAVA_HOME` and `ANDROID_HOME` to your installations; no machine-specific JDK path is committed.

```sh
./gradlew testDebugUnitTest lintDebug assembleDebug assembleRelease
```

On Windows, use `gradlew.bat`. The release is signed only when your ignored `keystore.properties` supplies your own signing configuration. CI produces a debug APK and an unsigned release APK. The GitHub beta release provides the maintainer-signed APK with checksums.

Release outputs:

- APK: `app/build/outputs/apk/release/app-release.apk`
- Unsigned release when no signing configuration exists: `app/build/outputs/apk/release/app-release-unsigned.apk`

The app only accepts IDX/Yahoo Jakarta symbols with `.JK`, such as `BBCA.JK`, `TLKM.JK`, and `ASII.JK`.
