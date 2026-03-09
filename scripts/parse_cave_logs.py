#!/usr/bin/env python3
"""Parse [CAVEDATA] lines from Minecraft server logs and produce statistics."""

import argparse
import json
import math
import re
import sys

CAVEDATA_RE = re.compile(
    r"\[CAVEDATA\]\s+"
    r"chunk=(-?\d+),(-?\d+)\s+"
    r"cave_blocks=(\d+)\s+"
    r"river_blocks=(\d+)\s+"
    r"river_liquid=(\d+)\s+"
    r"river_air=(\d+)\s+"
    r"seed=(-?\d+)"
)


def parse_log(path):
    entries = []
    with open(path, "r") as f:
        for line in f:
            m = CAVEDATA_RE.search(line)
            if m:
                entries.append({
                    "chunk_x": int(m.group(1)),
                    "chunk_z": int(m.group(2)),
                    "cave_blocks": int(m.group(3)),
                    "river_blocks": int(m.group(4)),
                    "river_liquid": int(m.group(5)),
                    "river_air": int(m.group(6)),
                    "seed": int(m.group(7)),
                })
    return entries


def stats(values):
    if not values:
        return {"mean": 0, "min": 0, "max": 0, "stdev": 0, "median": 0}
    n = len(values)
    mean = sum(values) / n
    variance = sum((v - mean) ** 2 for v in values) / n if n > 1 else 0
    stdev = math.sqrt(variance)
    sorted_v = sorted(values)
    median = sorted_v[n // 2] if n % 2 == 1 else (sorted_v[n // 2 - 1] + sorted_v[n // 2]) / 2
    return {
        "mean": round(mean, 2),
        "min": min(values),
        "max": max(values),
        "stdev": round(stdev, 2),
        "median": median,
    }


def analyze(entries):
    if not entries:
        return {"error": "No CAVEDATA entries found"}

    cave_vals = [e["cave_blocks"] for e in entries]
    river_vals = [e["river_blocks"] for e in entries]

    cave_stats = stats(cave_vals)
    river_stats = stats(river_vals)

    chunks_with_caves = sum(1 for v in cave_vals if v > 0)
    chunks_with_rivers = sum(1 for v in river_vals if v > 0)
    chunks_zero_caves = sum(1 for v in cave_vals if v == 0)

    cave_threshold = cave_stats["mean"] + 2 * cave_stats["stdev"]
    anomalous_chunks = [
        {"chunk_x": e["chunk_x"], "chunk_z": e["chunk_z"], "cave_blocks": e["cave_blocks"]}
        for e in entries
        if e["cave_blocks"] > cave_threshold and cave_stats["stdev"] > 0
    ]

    return {
        "total_chunks": len(entries),
        "seed": entries[0]["seed"],
        "cave_blocks": cave_stats,
        "river_blocks": river_stats,
        "chunks_with_caves": chunks_with_caves,
        "chunks_with_caves_pct": round(100 * chunks_with_caves / len(entries), 1),
        "chunks_with_rivers": chunks_with_rivers,
        "chunks_with_rivers_pct": round(100 * chunks_with_rivers / len(entries), 1),
        "chunks_zero_caves": chunks_zero_caves,
        "anomalous_high_density": anomalous_chunks,
        "anomalous_count": len(anomalous_chunks),
    }


def print_summary(report):
    if "error" in report:
        print(report["error"])
        return

    print(f"=== Cave Generation Report ===")
    print(f"Seed: {report['seed']}")
    print(f"Total chunks analyzed: {report['total_chunks']}")
    print()
    print(f"Cave blocks:  mean={report['cave_blocks']['mean']}  min={report['cave_blocks']['min']}  "
          f"max={report['cave_blocks']['max']}  stdev={report['cave_blocks']['stdev']}  "
          f"median={report['cave_blocks']['median']}")
    print(f"River blocks: mean={report['river_blocks']['mean']}  min={report['river_blocks']['min']}  "
          f"max={report['river_blocks']['max']}  stdev={report['river_blocks']['stdev']}  "
          f"median={report['river_blocks']['median']}")
    print()
    print(f"Chunks with caves:  {report['chunks_with_caves']} ({report['chunks_with_caves_pct']}%)")
    print(f"Chunks with rivers: {report['chunks_with_rivers']} ({report['chunks_with_rivers_pct']}%)")
    print(f"Chunks with zero caves: {report['chunks_zero_caves']}")
    print(f"Anomalous high-density chunks (>2 stdev): {report['anomalous_count']}")

    if report["anomalous_high_density"]:
        print("  Anomalous chunks:")
        for c in report["anomalous_high_density"][:10]:
            print(f"    chunk=({c['chunk_x']}, {c['chunk_z']}) cave_blocks={c['cave_blocks']}")


def main():
    parser = argparse.ArgumentParser(description="Parse cave generation log data")
    parser.add_argument("logfile", help="Path to Minecraft server log file")
    parser.add_argument("--output", "-o", help="Output JSON report path")
    args = parser.parse_args()

    entries = parse_log(args.logfile)
    report = analyze(entries)

    print_summary(report)

    if args.output:
        with open(args.output, "w") as f:
            json.dump(report, f, indent=2)
        print(f"\nJSON report written to: {args.output}")


if __name__ == "__main__":
    main()
