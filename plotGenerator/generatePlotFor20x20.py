
import matplotlib.pyplot as plt
import pandas as pd

# Read the data
df = pd.read_csv('updated_benchmark_results_20x20.csv')

# Separate data where both algorithms ran (non-negative times)
both_algos = df[(df['murty_time_sec'] > 0) & (df['ordergraph_time_sec'] > 0)]

# Convert to milliseconds
murty_ms = both_algos['murty_time_sec'] * 1000
ordergraph_ms = both_algos['ordergraph_time_sec'] * 1000

# Create single plot
plt.figure(figsize=(10, 6))

plt.plot(both_algos['k']/1000, murty_ms, 
         marker='o', color='purple', linewidth=2, markersize=4, label='Murty')
plt.plot(both_algos['k']/1000, ordergraph_ms, 
         marker='s', color='orange', linewidth=2, markersize=4, label='OrderGraph')

plt.xlabel('k (thousands)', fontsize=14)
plt.ylabel('Time (ms)', fontsize=14)
plt.title('20x20 Matrix: Runtime Comparison', fontsize=16)
plt.legend(fontsize=12)
plt.grid(True, alpha=0.3)

# Optional: Use log scale if there's a big difference in performance
# plt.yscale('log')
# plt.ylabel('Time (ms, log scale)', fontsize=14)

plt.tight_layout()
plt.savefig('updated_20x20_benchmark.png', dpi=100, bbox_inches='tight')
plt.show()