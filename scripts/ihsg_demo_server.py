"""Loopback-only, synthetic API fixtures for the IHSG Android demo.

No market data, AI provider, credentials, notifications or trading connection.
This is a UI demo server, not the production analysis backend.
"""

import argparse
import json
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import unquote, urlsplit

WATCHLIST = ["BBCA.JK", "TLKM.JK", "ASII.JK"]
QUOTES = {
    "BBCA.JK": ("DEMO · Bank Central Asia", 8600, 100, 1.18),
    "TLKM.JK": ("DEMO · Telkom Indonesia", 2800, -20, -0.71),
    "ASII.JK": ("DEMO · Astra International", 5100, 50, 0.99),
}
DEMO_REPORT = (
    "DEMO FIXTURE — synthetic data, not a market analysis or investment advice.\n\n"
    "This sample verifies that the Android client can display an IDX watchlist, "
    "quotes and a completed report. No live data or AI service was called."
)


class DemoHandler(BaseHTTPRequestHandler):
    def log_message(self, _format, *args):
        return

    def respond(self, payload, status=200):
        data = json.dumps(payload, ensure_ascii=False).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(data)))
        self.send_header("Cache-Control", "no-store")
        self.end_headers()
        self.wfile.write(data)

    def do_GET(self):
        path = unquote(urlsplit(self.path).path)
        if path == "/api/v1/health":
            return self.respond({"status": "demo fixture"})
        if path == "/api/v1/stocks/watchlist":
            return self.respond({"stock_codes": list(WATCHLIST)})
        if path.startswith("/api/v1/stocks/") and path.endswith("/quote"):
            symbol = path.split("/")[-2]
            if symbol not in QUOTES:
                return self.respond({"detail": "Demo supports BBCA.JK, TLKM.JK and ASII.JK only"}, 404)
            name, price, change, percent = QUOTES[symbol]
            return self.respond({
                "stock_code": symbol, "stock_name": name, "current_price": price,
                "change": change, "change_percent": percent, "open": price - change,
                "high": price + 25, "low": price - 50, "prev_close": price - change,
                "volume": 1000000, "update_time": "Synthetic demo data",
            })
        if path == "/api/v1/analysis/tasks":
            return self.respond({"tasks": [{
                "task_id": "demo-idx-report", "stock_code": "BBCA.JK",
                "status": "completed", "progress": 100, "message": "DEMO — synthetic fixture",
                "market_review_report": DEMO_REPORT,
            }]})
        if path == "/api/v1/history":
            return self.respond({"items": [{
                "query_id": "demo-history", "stock_code": "BBCA.JK", "stock_name": "DEMO fixture",
                "operation_advice": "Synthetic example only", "analysis_summary": DEMO_REPORT,
            }], "total": 1})
        return self.respond({"detail": "Not implemented by the UI demo fixture"}, 404)

    def do_POST(self):
        path = urlsplit(self.path).path
        if path in ("/api/v1/stocks/watchlist/add", "/api/v1/stocks/watchlist/remove"):
            return self.respond({"stock_codes": list(WATCHLIST)})
        if path in ("/api/v1/analysis", "/api/v1/analysis/market-review"):
            return self.respond({
                "task_id": "demo-idx-report", "stock_code": "BBCA.JK", "status": "completed",
                "progress": 100, "message": "DEMO — no analysis was run", "market_review_report": DEMO_REPORT,
            })
        return self.respond({"detail": "Not implemented by the UI demo fixture"}, 404)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--port", type=int, default=8000)
    args = parser.parse_args()
    server = ThreadingHTTPServer(("127.0.0.1", args.port), DemoHandler)
    print(f"IHSG UI demo fixtures on http://127.0.0.1:{args.port} — synthetic data only", flush=True)
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        pass
    finally:
        server.server_close()


if __name__ == "__main__":
    main()
