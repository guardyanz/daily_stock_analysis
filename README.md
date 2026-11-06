# Daily Stock Analysis — IDX / IHSG Edition

**Local stock-analysis tooling focused on Indonesia, with a native Android client.**

[![IHSG checks](https://github.com/guardyanz/daily_stock_analysis/actions/workflows/ihsg-checks.yml/badge.svg)](https://github.com/guardyanz/daily_stock_analysis/actions/workflows/ihsg-checks.yml)
[![MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Public beta](https://img.shields.io/badge/status-public_beta-yellow.svg)](https://github.com/guardyanz/daily_stock_analysis/releases)

This is an independent, MIT-licensed fork of [ZhuLinsen/Daily Stock Analysis](https://github.com/ZhuLinsen/daily_stock_analysis), configured for **IDX / BEI stocks**, the **IHSG index (`^JKSE`)**, rupiah context and the **Asia/Jakarta** market calendar. It retains the upstream Python/FastAPI analysis engine, web dashboard and desktop sources, and adds an Android client for the same backend.

**[Android beta and release notes](https://github.com/guardyanz/daily_stock_analysis/releases)** · [Setup and screenshots](docs/IHSG_EDITION.md) · [Android source](apps/ihsg-android) · [Upstream overview](README.upstream.md)

## What is specific to this edition?

- `.JK` stock symbols such as `BBCA.JK`, `TLKM.JK` and `ASII.JK`.
- Indonesia-specific analysis context: IHSG, BEI, rupiah and ARA/ARB.
- IDX daily-price routing through Yahoo Finance, with explicit market-profile and calendar regression tests.
- A Kotlin / Jetpack Compose Android console for watchlists, quotes, analysis jobs and report history.
- A credential-free synthetic demo for trying the Android UI and reproducing screenshots.

**Status: public beta.** The Android app is a client, not an on-device analysis engine. AI reports require a configured backend and your own supported provider credentials. Report output defaults to English; Bahasa Indonesia report generation is not currently claimed. This is research tooling, not trading execution or a promise of forecast accuracy.

## Try the Android UI without an API key

Start the synthetic demo server on your computer:

```sh
python scripts/ihsg_demo_server.py
```

Install the Android beta/debug build from Releases, or build it using [the Android guide](apps/ihsg-android/README.md). On an Android emulator, the default backend URL `http://10.0.2.2:8000` reaches the fixture server. A physical device can use `adb reverse tcp:8000 tcp:8000` and set the app's backend URL to `http://127.0.0.1:8000`.

All prices and reports in this demo are labelled synthetic. No market data, AI provider, account or notification service is used.

## Run the real backend

Use **Python 3.11** in a virtual environment:

```sh
git clone https://github.com/guardyanz/daily_stock_analysis.git
cd daily_stock_analysis
python -m venv .venv
# Activate .venv using your shell, then:
python -m pip install -r requirements.txt
cp .env.ihsg.example .env
```

On PowerShell, use `Copy-Item .env.ihsg.example .env`. Set a supported provider/model and your own API key in the ignored `.env`; the full configuration reference is [.env.example](.env.example).

```sh
python main.py --stocks BBCA.JK --dry-run --no-notify
python main.py --serve-only
```

The first command checks the data path without AI analysis or notifications. The server defaults to `127.0.0.1:8000`. To build the browser dashboard, run `npm ci` and `npm run build` in `apps/dsa-web`; the output goes to the ignored root `static/` directory.

For the Android beta, use a trusted local backend or an explicit ADB tunnel. The native client does not implement the upstream browser password-login session. Do not expose an unauthenticated backend to the public internet. See [deployment and beta limitations](docs/IHSG_EDITION.md).

## Verify and contribute

```sh
python -m pip install -r .github/requirements-ci.txt
python -m pytest tests/test_idx_market_support.py tests/test_ihsg_demo_server.py -q
python scripts/check_ai_assets.py
```

For broader backend validation, use `python -m pytest -m "not network"`. Android build, lint, unit tests and emulator screenshots run in [IHSG Edition checks](.github/workflows/ihsg-checks.yml). Improvements to IDX data provenance, trading calendars, API/client compatibility and Bahasa Indonesia documentation are welcome.

## Upstream and license

The engine is derived from [ZhuLinsen/Daily Stock Analysis](https://github.com/ZhuLinsen/daily_stock_analysis) under [MIT](LICENSE). Existing author credits and copyright notices remain. This fork is not endorsed by the upstream maintainers, BEI, OJK or any data provider. Upstream documentation and broader market features remain in [README.upstream.md](README.upstream.md); edition-specific behavior is documented in [docs/IHSG_EDITION.md](docs/IHSG_EDITION.md).

**Bahasa Indonesia:** Edisi ini berfokus pada saham BEI dan IHSG. Backend berjalan di komputer/server Anda; Android mengakses backend tersebut. Mulai dari demo sintetis tanpa API key, lalu konfigurasi penyedia AI sendiri untuk analisis nyata. Data dan keluaran perlu diperiksa secara mandiri.
