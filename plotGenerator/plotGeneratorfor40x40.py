import matplotlib.pyplot as plt
import pandas as pd

# Read the data
df = pd.read_csv('updated_benchmark_results_40x40.csv')

# Convert to milliseconds
murty_ms = df['murty_time_sec'] * 1000
ordergraph_ms = df['ordergraph_time_sec'] * 1000

# Create single plot
plt.figure(figsize=(10, 6))

plt.plot(df['k']/1000, murty_ms, 
         marker='o', color='purple', linewidth=2, markersize=4, label='Murty')
plt.plot(df['k']/1000, ordergraph_ms, 
         marker='s', color='orange', linewidth=2, markersize=4, label='OrderGraph')

plt.xlabel('k (thousands)', fontsize=14)
plt.ylabel('Time (ms)', fontsize=14)
plt.title('40x40 Matrix: Runtime Comparison', fontsize=16)
plt.legend(fontsize=12)
plt.grid(True, alpha=0.3)

plt.tight_layout()
plt.savefig('updated_40x40_benchmark.png', dpi=100, bbox_inches='tight')
plt.show()