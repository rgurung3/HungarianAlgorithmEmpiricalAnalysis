import matplotlib.pyplot as plt
import pandas as pd
import numpy as np

# Read the fixed k benchmark file
df = pd.read_csv('updated_benchmark_fixed_k_results.csv')

# Extract data for k=100,000
df_100k = df[df['k'] == 100000]
k_100k_sizes = df_100k['n'].values
k_100k_murty = df_100k['murty_time_sec'].values
k_100k_ordergraph = df_100k['ordergraph_time_sec'].values

# Extract data for k=250,000
df_250k = df[df['k'] == 250000]
k_250k_sizes = df_250k['n'].values
k_250k_murty = df_250k['murty_time_sec'].values
k_250k_ordergraph = df_250k['ordergraph_time_sec'].values

# Create two subplots
fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(14, 6))

# Plot for k=100,000
ax1.plot(k_100k_sizes, k_100k_murty, 
         marker='o', color='purple', linewidth=2, markersize=6, label='Murty')
ax1.plot(k_100k_sizes, k_100k_ordergraph, 
         marker='s', color='orange', linewidth=2, markersize=6, label='OrderGraph')

# Mark crossover region if it exists
for i in range(len(k_100k_sizes)-1):
    if k_100k_murty[i] > k_100k_ordergraph[i] and k_100k_murty[i+1] < k_100k_ordergraph[i+1]:
        ax1.axvspan(k_100k_sizes[i], k_100k_sizes[i+1], alpha=0.2, color='red')
        ax1.text((k_100k_sizes[i] + k_100k_sizes[i+1])/2, 
                max(max(k_100k_murty), max(k_100k_ordergraph))*0.9,
                'Crossover\nRegion', ha='center', fontsize=10)

ax1.set_xlabel('Matrix Size (n×n)', fontsize=12)
ax1.set_ylabel('Time (seconds)', fontsize=12)
ax1.set_title('Performance at k=100,000', fontsize=14)
ax1.legend(fontsize=11)
ax1.grid(True, alpha=0.3)
ax1.set_xticks(k_100k_sizes)  # Show all ticks
ax1.set_xticklabels(k_100k_sizes)  # Force all labels to show

# Plot for k=250,000
ax2.plot(k_250k_sizes, k_250k_murty, 
         marker='o', color='purple', linewidth=2, markersize=6, label='Murty')
ax2.plot(k_250k_sizes, k_250k_ordergraph, 
         marker='s', color='orange', linewidth=2, markersize=6, label='OrderGraph')

# Mark crossover region if it exists
for i in range(len(k_250k_sizes)-1):
    if k_250k_murty[i] > k_250k_ordergraph[i] and k_250k_murty[i+1] < k_250k_ordergraph[i+1]:
        ax2.axvspan(k_250k_sizes[i], k_250k_sizes[i+1], alpha=0.2, color='red')
        ax2.text((k_250k_sizes[i] + k_250k_sizes[i+1])/2, 
                max(max(k_250k_murty), max(k_250k_ordergraph))*0.9,
                'Crossover\nRegion', ha='center', fontsize=10)

ax2.set_xlabel('Matrix Size (n×n)', fontsize=12)
ax2.set_ylabel('Time (seconds)', fontsize=12)
ax2.set_title('Performance at k=250,000', fontsize=14)
ax2.legend(fontsize=11)
ax2.grid(True, alpha=0.3)
ax2.set_xticks(k_250k_sizes)  # Show all ticks
ax2.set_xticklabels(k_250k_sizes)  # Force all labels to show

plt.suptitle('Algorithm Performance vs Matrix Size (Fixed k)', fontsize=16)
plt.tight_layout()
plt.savefig('updated_performance_vs_matrix_size.png', dpi=100, bbox_inches='tight')
plt.show()