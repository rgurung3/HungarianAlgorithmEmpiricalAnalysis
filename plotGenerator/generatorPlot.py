import matplotlib.pyplot as plt
import numpy as np

# Your actual data
ks = [1000, 5000, 10000, 25000, 50000, 100000, 500000, 1000000, 2000000, 3000000, 3700000]
murty_times = [0.054676, 0.108168, 0.137859, 0.349762, 0.720921, 1.043332, 4.704579, 9.377326, 17.799771, 26.222942, 31.033860]
ordertree_times = [0.068629, 0.124924, 0.196295, 0.452671, 0.688772, 1.337356, 4.752478, 8.011132, 13.956035, 19.073901, 21.462895]

# Convert to milliseconds for easier reading
murty_times_ms = [t * 1000 for t in murty_times]
ordertree_times_ms = [t * 1000 for t in ordertree_times]

# Plot 1: Log-log plot comparing both algorithms
plt.figure(figsize=(12, 7))
plt.plot(ks, murty_times_ms, marker='o', linestyle='-', color='purple', label='Murty', markersize=8)
plt.plot(ks, ordertree_times_ms, marker='s', linestyle='-', color='orange', label='OrderTree', markersize=8)
plt.xscale('log')
plt.yscale('log')
plt.title("Murty vs OrderTree Runtime Comparison (10x10 matrices)", fontsize=16)
plt.xlabel("k (log scale)", fontsize=14)
plt.ylabel("Time (ms, log scale)", fontsize=14)
plt.legend(fontsize=12)
plt.grid(True, which="both", ls="-", alpha=0.2)
plt.savefig("murty_ordertree_runtime_loglog.png", dpi=100, bbox_inches='tight')
plt.show()

# Plot 2: Small k values (linear scale)
plt.figure(figsize=(12, 7))
plt.plot(ks[:6], murty_times_ms[:6], marker='o', linestyle='-', color='purple', label='Murty', markersize=8)
plt.plot(ks[:6], ordertree_times_ms[:6], marker='s', linestyle='-', color='orange', label='OrderTree', markersize=8)
plt.title("Murty vs OrderTree Runtime (k = 1,000 to 100,000)", fontsize=16)
plt.xlabel("k", fontsize=14)
plt.ylabel("Time (ms)", fontsize=14)
plt.legend(fontsize=12)
plt.grid(True, alpha=0.3)
plt.savefig("murty_ordertree_runtime_small.png", dpi=100, bbox_inches='tight')
plt.show()

# Plot 3: Large k values (linear scale)
plt.figure(figsize=(12, 7))
plt.plot(ks[6:], murty_times_ms[6:], marker='o', linestyle='-', color='purple', label='Murty', markersize=8)
plt.plot(ks[6:], ordertree_times_ms[6:], marker='s', linestyle='-', color='orange', label='OrderTree', markersize=8)
plt.title("Murty vs OrderTree Runtime (k = 500,000 to 3,700,000)", fontsize=16)
plt.xlabel("k", fontsize=14)
plt.ylabel("Time (ms)", fontsize=14)
plt.legend(fontsize=12)
plt.grid(True, alpha=0.3)
plt.savefig("murty_ordertree_runtime_large.png", dpi=100, bbox_inches='tight')
plt.show()

# Plot 4: Speedup ratio
speedup = [m/o for m, o in zip(murty_times, ordertree_times)]
plt.figure(figsize=(12, 7))
plt.plot(ks, speedup, marker='d', linestyle='-', color='green', markersize=8)
plt.axhline(y=1, color='r', linestyle='--', alpha=0.5, label='Equal performance')
plt.xscale('log')
plt.title("Speedup: Murty Time / OrderTree Time (>1 means OrderTree is faster)", fontsize=16)
plt.xlabel("k (log scale)", fontsize=14)
plt.ylabel("Speedup Ratio", fontsize=14)
plt.legend(fontsize=12)
plt.grid(True, alpha=0.3)
plt.savefig("speedup_ratio.png", dpi=100, bbox_inches='tight')
plt.show()