# Contributing to the IDX / IHSG Edition

Use English for public issues, pull requests and edition documentation. Start with the [edition guide](docs/IHSG_EDITION.md) and keep upstream attribution and MIT notices.

Useful contributions include IDX symbol handling, market-calendar fixtures, data timestamps, Android API compatibility and accessibility. Describe the observed behavior, expected result and steps to reproduce. Use synthetic examples and remove provider keys, personal portfolios and private reports from attachments.

Use Python 3.11 in a virtual environment. Run `python -m pytest tests/test_idx_market_support.py tests/test_ihsg_demo_server.py -q` and `python scripts/check_ai_assets.py`. For Android changes, use JDK 17 and run `./gradlew testDebugUnitTest lintDebug assembleDebug` in `apps/ihsg-android`. The edition workflow also exercises the real Android app against deterministic demo fixtures.

Keep changes focused and document user-visible behavior in the edition guide and changelog. Do not commit `.env`, signing keys, generated APKs or screenshots containing private information. Financial outputs should preserve their data source, timestamp and uncertainty.
