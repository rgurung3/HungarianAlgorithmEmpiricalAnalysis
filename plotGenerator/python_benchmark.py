import numpy as np
import csv
import math
from enumerator import enumerator
from timer import Timer

def load_matrix(seed, n=10):
    return np.loadtxt(f"../cost_matrix_{seed}.txt", dtype=int)

def run_benchmark(ks, num_trials=1, n=10):
    with open("../python_results.csv", mode="w", newline="") as f:
        writer = csv.writer(f)
        writer.writerow(["matrix_id", "k", "avg_time_sec", "avg_hungarian_calls"])

        for k in ks:
            total_time = 0.0
            total_calls = 0

            for seed in range(num_trials):
                matrix = load_matrix(seed, n)

                # Run enumerator and collect timer info
                _, timer = enumerator(matrix, k)
                total_time += timer.total_time
                total_calls += timer.total_calls

            avg_time_sec = total_time / num_trials
            avg_calls = total_calls / num_trials
            row = [0, k, f"{avg_time_sec:.6f}", f"{avg_calls:.1f}"]
            writer.writerow(row)
            print(",".join(map(str, row)))

if __name__ == "__main__":
    ks = [1000, 5000, 10000, 25000, 50000, 100000, 500000, 1000000, 2000000, 3000000, 3700000]
    run_benchmark(ks)
