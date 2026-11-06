"""The documentation demo must stay deterministic and credential-free."""

import importlib.util
import json
from pathlib import Path
from threading import Thread
from urllib.request import urlopen


def test_android_demo_api_contract():
    path = Path(__file__).resolve().parents[1] / "scripts" / "ihsg_demo_server.py"
    spec = importlib.util.spec_from_file_location("ihsg_demo", path)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    server = module.ThreadingHTTPServer(("127.0.0.1", 0), module.DemoHandler)
    thread = Thread(target=server.serve_forever, daemon=True)
    thread.start()
    try:
        base = f"http://127.0.0.1:{server.server_port}"

        def get(endpoint):
            with urlopen(base + endpoint, timeout=2) as response:
                return json.load(response)

        assert get("/api/v1/health")["status"] == "demo fixture"
        symbols = get("/api/v1/stocks/watchlist")["stock_codes"]
        assert symbols == ["BBCA.JK", "TLKM.JK", "ASII.JK"]
        for symbol in symbols:
            quote = get(f"/api/v1/stocks/{symbol}/quote")
            assert quote["stock_code"] == symbol
            assert quote["stock_name"].startswith("DEMO")
            assert quote["current_price"] > 0
        task = get("/api/v1/analysis/tasks")["tasks"][0]
        assert task["status"] == "completed"
        assert "synthetic data" in task["market_review_report"]
        assert get("/api/v1/history")["items"][0]["query_id"] == "demo-history"
    finally:
        server.shutdown()
        server.server_close()
        thread.join(timeout=2)
