"""Capture the real IHSG Android UI against the local synthetic fixture server."""

import argparse
import json
from pathlib import Path
import re
import subprocess
import time


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output", type=Path, default=Path("artifacts/ihsg-screenshots"))
    parser.add_argument("--package", default="id.ihsg.stockanalysis.debug")
    args = parser.parse_args()
    args.output.mkdir(parents=True, exist_ok=True)

    def adb(*command):
        return subprocess.check_output(["adb", *command], text=True, encoding="utf-8").strip()

    resolution = adb("shell", "wm", "size")
    size = re.search(r"Physical size:\s*(\d+)x(\d+)", resolution)
    if size is None:
        raise RuntimeError(f"Unable to calibrate emulator resolution: {resolution}")
    width, height = map(int, size.groups())
    adb("shell", "settings", "put", "global", "window_animation_scale", "0")
    adb("shell", "settings", "put", "global", "transition_animation_scale", "0")
    adb("shell", "settings", "put", "global", "animator_duration_scale", "0")
    adb("shell", "am", "start", "-W", "-n", f"{args.package}/id.ihsg.stockanalysis.MainActivity")

    def dump(name):
        adb("shell", "uiautomator", "dump", "/sdcard/ihsg-window.xml")
        content = adb("shell", "cat", "/sdcard/ihsg-window.xml")
        (args.output / f"{name}.xml").write_text(content, encoding="utf-8")
        return content

    for _ in range(24):
        time.sleep(1)
        content = dump("dashboard")
        if "demo fixture" in content and "BBCA.JK" in content:
            break
    else:
        raise RuntimeError("Expected the running app to show connected demo fixtures and BBCA.JK")

    def screenshot(name):
        remote = f"/sdcard/{name}.png"
        adb("shell", "screencap", "-p", remote)
        adb("pull", remote, str(args.output / f"{name}.png"))

    screenshot("android-dashboard")
    adb("shell", "input", "swipe", str(width // 2), str(height * 4 // 5),
        str(width // 2), str(height // 4), "600")
    time.sleep(2)
    content = dump("watchlist")
    if "IHSG Stock Analysis" not in content:
        raise RuntimeError("App left the expected screen during capture")
    screenshot("android-watchlist")
    metadata = {
        "source": "Real Android emulator UI; synthetic loopback API fixtures",
        "package": args.package, "resolution": resolution,
        "android": adb("shell", "getprop", "ro.build.version.release"),
        "device": adb("shell", "getprop", "ro.product.model"),
        "data": "Not live prices, not AI-generated financial analysis",
    }
    (args.output / "capture.json").write_text(json.dumps(metadata, indent=2), encoding="utf-8")
    print(json.dumps(metadata))


if __name__ == "__main__":
    main()
