#!/usr/bin/env python3
from pathlib import Path
import re
import pandas as pd
import matplotlib.pyplot as plt

def load_with_n(path: Path) -> pd.DataFrame:
    df = pd.read_csv(path)
    df.columns = [c.strip().lower() for c in df.columns]
    if "n" not in df.columns:
        m = re.search(r'_(\d+)x\1', path.name)
        if m:
            df["n"] = int(m.group(1))
    # enforce numeric
    for c in ("k", "n"):
        if c in df.columns:
            df[c] = pd.to_numeric(df[c], errors="coerce")
    if "murty_time_sec" in df.columns:
        df["murty_time_sec"] = pd.to_numeric(df["murty_time_sec"], errors="coerce")
    return df[["n", "k", "murty_time_sec"]]

def main():
    HERE = Path(__file__).resolve().parent

    # Original (before changes) CSVs
    orig_names = [
        "benchmark_results_10x10.csv",
        "benchmark_results_20x20.csv",
        "benchmark_results_25x25.csv",
        "benchmark_results_30x30.csv",
        "benchmark_results_40x40.csv",
    ]
    # Updated (after changes) CSVs — adjust to "updated_benchmark_results_*.csv" if needed
    upd_names = [
        "updated2_benchmark_results_10x10.csv",
        "updated2_benchmark_results_20x20.csv",
        "updated2_benchmark_results_25x25.csv",
        "updated2_benchmark_results_30x30.csv",
        "updated2_benchmark_results_40x40.csv",
    ]

    orig_paths = [HERE / n for n in orig_names if (HERE / n).exists()]
    upd_paths  = [HERE / n for n in upd_names if (HERE / n).exists()]

    if not orig_paths:
        raise FileNotFoundError("No original CSVs found next to the script.")
    if not upd_paths:
        raise FileNotFoundError("No updated CSVs found next to the script (check filenames).")

    orig_df = pd.concat([load_with_n(p) for p in orig_paths], ignore_index=True)
    upd_df  = pd.concat([load_with_n(p) for p in upd_paths],  ignore_index=True)

    merged = pd.merge(
        orig_df.rename(columns={"murty_time_sec": "murty_orig"}),
        upd_df.rename(columns={"murty_time_sec": "murty_upd"}),
        on=["n", "k"],
        how="inner"
    )

    if merged.empty:
        raise ValueError("No overlapping (n, k) rows between original and updated CSVs. "
                         "Check that both sets were generated with the same k values.")

    merged["delta_sec"] = merged["murty_upd"] - merged["murty_orig"]

    # Aggregate by n (ignore k in the final report)
    summary = (merged
        .groupby("n", as_index=False)
        .agg(rows=("k", "count"),
             murty_time_orig_total=("murty_orig", "sum"),
             murty_time_upd_total=("murty_upd", "sum"),
             murty_delta_total_sec=("delta_sec", "sum"),
             murty_delta_mean_sec=("delta_sec", "mean"),
             murty_delta_median_sec=("delta_sec", "median"))
    )
    summary["murty_speedup_total_x"] = summary["murty_time_orig_total"] / summary["murty_time_upd_total"]
    summary["murty_pct_change_total"] = (
        (summary["murty_time_upd_total"] - summary["murty_time_orig_total"]) /
        summary["murty_time_orig_total"]
    )

    out_csv = HERE / "murty_only_diffs_by_n.csv"
    summary.to_csv(out_csv, index=False)
    print(f"Wrote {out_csv}")

    plots_dir = HERE / "plots"
    plots_dir.mkdir(exist_ok=True)

    # Plot 1: total Δ time by n (updated - original). Negative = faster after changes
    plt.figure()
    plt.bar(summary["n"].astype(str), summary["murty_delta_total_sec"])
    plt.axhline(0.0, linestyle="--")
    plt.xlabel("n")
    plt.ylabel("Total Δ time (sec)  updated − original")
    plt.title("Murty: total time difference by n (lower is better)")
    p1 = plots_dir / "murty_total_delta_by_n.png"
    plt.savefig(p1, bbox_inches="tight", dpi=160)
    plt.close()
    print(f"Wrote {p1}")

    # Plot 2: total speedup by n (>1 = updated faster)
    plt.figure()
    plt.bar(summary["n"].astype(str), summary["murty_speedup_total_x"])
    plt.axhline(1.0, linestyle="--")
    plt.xlabel("n")
    plt.ylabel("Total speedup (original / updated)")
    plt.title("Murty: total speedup by n (>1 is better)")
    p2 = plots_dir / "murty_total_speedup_by_n.png"
    plt.savefig(p2, bbox_inches="tight", dpi=160)
    plt.close()
    print(f"Wrote {p2}")

if __name__ == "__main__":
    main()
