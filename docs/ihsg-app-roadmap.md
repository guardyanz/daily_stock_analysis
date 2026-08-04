# IHSG App Roadmap

This fork is being shaped into an Indonesia-first stock analysis app.

## Product Scope

- Primary market: Indonesia Stock Exchange / BEI.
- Primary index: IHSG / IDX Composite via Yahoo Finance symbol `^JKSE`.
- Stock symbol format: Yahoo Finance IDX suffix, for example `BBCA.JK`, `TLKM.JK`, `ASII.JK`.
- Default analysis context: rupiah, Bank Indonesia rates, foreign flow, OJK/BEI policy, tick size, and ARA/ARB.
- Out of scope for the first IHSG edition: A-share-only concepts such as Northbound flow, Dragon Tiger list, and China-specific daily limit boards.

## Windows App

- Reuse the existing Electron desktop shell in `apps/dsa-desktop`.
- Keep FastAPI/Python as the local backend.
- Rebrand desktop packaging to IHSG edition after the data path is stable.
- Default onboarding should ask for API key, report language, and IDX watchlist only.

## Android App

- Build Android as a client for the same FastAPI contract, not as a separate analysis engine.
- Recommended first native path: Kotlin + Jetpack Compose for durable Android UX.
- Alternative cross-platform path: Flutter if shared mobile UI becomes more important than native Android polish.
- Keep offline cache limited to watchlist, latest reports, and portfolio snapshots.

## UI Direction

- Use a dense dashboard for repeated daily work, not a marketing-style landing page.
- First screen: IHSG status, watchlist, latest analysis, portfolio risk, and recent alerts.
- Use compact cards only for repeated entities such as stocks, reports, and alerts.
- Prefer Bahasa Indonesia labels for product flows, with English technical market terms when traders commonly use them.
