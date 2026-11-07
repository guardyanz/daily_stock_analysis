# IDX / IHSG Edition

This public beta retains the upstream MIT analysis engine and adds IDX market configuration and an Android console. It is an independent fork; the upstream project's hosted site is not a deployment of this edition.

## Quick setup

1. Use Python 3.11 and install `requirements.txt` in a virtual environment.
2. Copy `.env.ihsg.example` to `.env` and supply a supported AI provider/model and your own key. Never commit `.env`.
3. Keep `STOCK_LIST` in `.JK` notation and `MARKET_REVIEW_REGION=id`.
4. Start `python main.py --serve-only` for the API. Build `apps/dsa-web` with `npm ci` and `npm run build` when the browser UI is needed.
5. Build/install the Android client from `apps/ihsg-android`. It connects to the same FastAPI backend.

The Android beta cannot sign in to the browser's password-login session. Use a trusted local backend and ADB tunnelling for this beta. If authentication is enabled on the upstream backend, use the web client instead. An unauthenticated local API must not be exposed on the public internet.

## Android UI demo and screenshots

`python scripts/ihsg_demo_server.py` starts a loopback-only, stdlib fixture server. It returns fixed, synthetic watchlist quotes and reports; it neither fetches market data nor runs an LLM. Only BBCA.JK, TLKM.JK and ASII.JK fixtures are supported. Watchlist mutations are demonstration no-ops.

On an emulator, use `http://10.0.2.2:8000`. On a USB-connected device:

```sh
adb reverse tcp:8000 tcp:8000
```

Set the app URL to `http://127.0.0.1:8000`. Remove the reverse tunnel after testing with `adb reverse --remove tcp:8000`.

The `IHSG Edition checks` workflow builds the real debug APK, starts these fixtures and captures the app on an Android emulator. Download `ihsg-android-beta-evidence` for screenshots, UI XML, build reports and capture metadata. Long-lived screenshots in this guide are copied from verified workflow artifacts; they depict synthetic values, not current prices or investment advice.

To reproduce locally with an already running emulator, debug APK and fixture server:

```sh
adb install -r apps/ihsg-android/app/build/outputs/apk/debug/app-debug.apk
python scripts/capture_ihsg_screenshots.py
```

The script calibrates resolution and checks the connected fixture state before capturing. One-time outputs stay under ignored `artifacts/`; only selected product documentation images are committed.

## Current limits

- IDX prices use the Yahoo Finance data path and can be delayed, incomplete or unavailable. Preserve timestamps and verify important values against your data source.
- Android is an online client of your backend. It does not contain a financial model, a local LLM, trading execution or a broker integration.
- Default report language is English. Native controls contain Indonesian and English labels.
- The inherited trading-calendar and ARA/ARB context are tooling inputs, not a complete statement of current exchange regulations.
- Device screenshots verify the displayed flow, not live data reliability or predictive accuracy.
- Scheduled analysis, provider billing and notifications require your own deliberate configuration; the demo never enables them.
- The inherited daily-analysis schedule is opt-in for this edition. Set the GitHub repository variable `ENABLE_SCHEDULED_ANALYSIS=true` to enable it, or run that workflow manually.

## Provenance

Initial edition changes were based on commit `3c3bfcbd373d40dbda9a85c4f69801311e23d6df`; the previous upstream overview is preserved in `README.upstream.md`. MIT copyright notices remain in LICENSE. The main README is English-first with an Indonesian summary; the original Chinese upstream documentation remains available separately, since it describes broader markets and upstream deployment rather than this edition.
