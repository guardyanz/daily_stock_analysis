# -*- coding: utf-8 -*-
"""Regression tests for Indonesia IDX / IHSG market support."""

from unittest.mock import patch
import sys
from unittest.mock import MagicMock

import pandas as pd

if "fake_useragent" not in sys.modules:
    sys.modules["fake_useragent"] = MagicMock()

from data_provider.base import BaseFetcher, DataFetchError, DataFetcherManager, normalize_stock_code
from data_provider.yfinance_fetcher import YfinanceFetcher
from src.core.market_profile import get_profile
from src.core.trading_calendar import (
    MARKET_EXCHANGE,
    MARKET_TIMEZONE,
    compute_effective_region,
    get_market_for_stock,
)
from src.market_context import detect_market, get_market_guidelines
from src.services.stock_code_utils import is_code_like, normalize_code


class _FakeFetcher(BaseFetcher):
    def __init__(self, name: str, should_fail: bool = False):
        self.name = name
        self.priority = 0 if name != "YfinanceFetcher" else 4
        self.calls = []
        self.should_fail = should_fail

    def _fetch_raw_data(self, stock_code: str, start_date: str, end_date: str) -> pd.DataFrame:
        raise NotImplementedError

    def _normalize_data(self, df: pd.DataFrame, stock_code: str) -> pd.DataFrame:
        raise NotImplementedError

    def get_daily_data(self, stock_code, start_date=None, end_date=None, days=30):
        self.calls.append(stock_code)
        if self.should_fail:
            raise DataFetchError(f"{self.name} should not be called for {stock_code}")
        return pd.DataFrame(
            {
                "date": [pd.Timestamp("2026-07-03")],
                "open": [1.0],
                "high": [1.0],
                "low": [1.0],
                "close": [1.0],
                "volume": [100],
                "amount": [100.0],
                "pct_chg": [0.0],
            }
        )


def test_normalize_and_detect_idx_suffix_codes() -> None:
    assert normalize_stock_code("bbca.jk") == "BBCA.JK"
    assert normalize_stock_code("tlkm.JK") == "TLKM.JK"

    assert detect_market("BBCA.JK") == "id"
    assert get_market_for_stock("BBCA.JK") == "id"

    assert is_code_like("BBCA.JK") is True
    assert normalize_code("BBCA.JK") == "BBCA.JK"


def test_market_guidelines_for_idx_are_indonesia_specific() -> None:
    guidelines = get_market_guidelines("BBCA.JK")

    assert "IHSG" in guidelines
    assert "BEI" in guidelines
    assert "ARA/ARB" in guidelines
    assert "rupiah" in guidelines.lower()


def test_yfinance_keeps_idx_suffix_codes_and_ihsg_index() -> None:
    fetcher = YfinanceFetcher()

    assert fetcher._convert_stock_code("BBCA.JK") == "BBCA.JK"
    assert fetcher._convert_stock_code("^JKSE") == "^JKSE"

    captured = []

    def fake_fetch(_yf, yf_code, name, return_code):
        captured.append((yf_code, name, return_code))
        return {"code": return_code, "name": name, "current": 1.0}

    fetcher._fetch_yf_ticker_data = fake_fetch  # type: ignore[method-assign]

    with patch.dict(sys.modules, {"yfinance": MagicMock()}):
        idx_indices = fetcher.get_main_indices("id") or []

    assert {item["code"] for item in idx_indices} == {"JKSE"}
    assert ("^JKSE", "IHSG", "JKSE") in captured


def test_data_fetcher_manager_routes_idx_daily_only_to_yfinance() -> None:
    efinance = _FakeFetcher("EfinanceFetcher", should_fail=True)
    akshare = _FakeFetcher("AkshareFetcher", should_fail=True)
    yfinance = _FakeFetcher("YfinanceFetcher")
    manager = DataFetcherManager(fetchers=[efinance, akshare, yfinance])

    with patch("data_provider.base.record_provider_run_started"), patch("data_provider.base.record_provider_run"):
        idx_df, idx_source = manager.get_daily_data("BBCA.JK")

    assert idx_source == "YfinanceFetcher"
    assert not idx_df.empty
    assert efinance.calls == []
    assert akshare.calls == []
    assert yfinance.calls == ["BBCA.JK"]


def test_trading_calendar_registers_idx_exchange_and_timezone() -> None:
    assert MARKET_EXCHANGE["id"] == "XIDX"
    assert MARKET_TIMEZONE["id"] == "Asia/Jakarta"


def test_market_review_supports_idx_region_profile_and_calendar_filter() -> None:
    assert get_profile("id").mood_index_code == "JKSE"

    assert compute_effective_region("id", {"id"}) == "id"
    assert compute_effective_region("id", set()) == ""
    assert compute_effective_region("both", {"id"}) == "id"
