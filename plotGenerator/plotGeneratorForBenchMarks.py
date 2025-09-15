import pandas as pd
import matplotlib.pyplot as plt
import numpy as np

# Load the 10x10 datasets
java_10 = pd.read_csv("java_results.csv")
python_10 = pd.read_csv("python_results.csv")

# Add source column
java_10['source'] = 'Java'
python_10['source'] = 'Python'

# Combine dataframes
df_10x10 = pd.concat([java_10, python_10], ignore_index=True)

# Ensure numeric types
df_10x10['avg_time_sec'] = df_10x10['avg_time_sec'].astype(float)
df_10x10['avg_hungarian_calls'] = df_10x10['avg_hungarian_calls'].astype(float)

# Create figure with 2x2 subplots
fig, axes = plt.subplots(2, 2, figsize=(16, 12))

# Plot 1: Runtime (log-log scale)
ax1 = axes[0, 0]
for source in ['Java', 'Python']:
    subset = df_10x10[df_10x10['source'] == source]
    ax1.plot(subset['k'], subset['avg_time_sec'], marker='o', label=source, linewidth=2, markersize=8)
ax1.set_xscale('log')
ax1.set_yscale('log')
ax1.set_xlabel('k (number of assignments)')
ax1.set_ylabel('Average Time (seconds)')
ax1.set_title('Runtime Comparison: Java vs Python (10x10, Log-Log Scale)')
ax1.legend()
ax1.grid(True, alpha=0.3)

# Plot 2: Hungarian Calls (log-log scale)
ax2 = axes[0, 1]
for source in ['Java', 'Python']:
    subset = df_10x10[df_10x10['source'] == source]
    ax2.plot(subset['k'], subset['avg_hungarian_calls'], marker='s', label=source, linewidth=2, markersize=8)
ax2.set_xscale('log')
ax2.set_yscale('log')
ax2.set_xlabel('k (number of assignments)')
ax2.set_ylabel('Average Hungarian Calls')
ax2.set_title('Hungarian Algorithm Calls: Java vs Python (10x10, Log-Log Scale)')
ax2.legend()
ax2.grid(True, alpha=0.3)

# Plot 3: Runtime Ratio (Java/Python)
ax3 = axes[1, 0]
java_data = df_10x10[df_10x10['source'] == 'Java'].sort_values('k')
python_data = df_10x10[df_10x10['source'] == 'Python'].sort_values('k')
time_ratio = java_data['avg_time_sec'].values / python_data['avg_time_sec'].values
ax3.plot(java_data['k'].values, time_ratio, marker='o', color='red', linewidth=2, markersize=8)
ax3.set_xscale('log')
ax3.set_xlabel('k (number of assignments)')
ax3.set_ylabel('Time Ratio (Java/Python)')
ax3.set_title('Performance Ratio: How many times slower is Java?')
ax3.grid(True, alpha=0.3)
# Add horizontal line at y=1 for reference
ax3.axhline(y=1, color='black', linestyle='--', alpha=0.5, label='Equal performance')
ax3.legend()

# Plot 4: Hungarian Calls Ratio (Java/Python)
ax4 = axes[1, 1]
calls_ratio = java_data['avg_hungarian_calls'].values / python_data['avg_hungarian_calls'].values
ax4.plot(java_data['k'].values, calls_ratio, marker='s', color='purple', linewidth=2, markersize=8)
ax4.set_xscale('log')
ax4.set_xlabel('k (number of assignments)')
ax4.set_ylabel('Calls Ratio (Java/Python)')
ax4.set_title('Hungarian Calls Ratio: How many more calls does Java make?')
ax4.grid(True, alpha=0.3)
ax4.axhline(y=1, color='black', linestyle='--', alpha=0.5, label='Equal calls')
ax4.legend()

plt.tight_layout()
plt.savefig("benchmark_comparison_10x10.png", dpi=300, bbox_inches='tight')
plt.show()

# Create individual plots for better readability
# Plot 1: Runtime comparison only
plt.figure(figsize=(10, 6))
for source in ['Java', 'Python']:
    subset = df_10x10[df_10x10['source'] == source]
    plt.plot(subset['k'], subset['avg_time_sec'], marker='o', label=source, linewidth=2, markersize=8)
plt.xscale('log')
plt.yscale('log')
plt.xlabel('k (number of assignments)', fontsize=12)
plt.ylabel('Average Time (seconds)', fontsize=12)
plt.title('Runtime Comparison: Java vs Python (10x10 Matrix)', fontsize=14)
plt.legend(fontsize=12)
plt.grid(True, alpha=0.3)
plt.tight_layout()
plt.savefig("runtime_comparison_10x10.png", dpi=300, bbox_inches='tight')
plt.show()

# Plot 2: Hungarian calls comparison only
plt.figure(figsize=(10, 6))
for source in ['Java', 'Python']:
    subset = df_10x10[df_10x10['source'] == source]
    plt.plot(subset['k'], subset['avg_hungarian_calls'], marker='s', label=source, linewidth=2, markersize=8)
plt.xscale('log')
plt.yscale('log')
plt.xlabel('k (number of assignments)', fontsize=12)
plt.ylabel('Average Hungarian Algorithm Calls', fontsize=12)
plt.title('Hungarian Algorithm Calls: Java vs Python (10x10 Matrix)', fontsize=14)
plt.legend(fontsize=12)
plt.grid(True, alpha=0.3)
plt.tight_layout()
plt.savefig("hungarian_calls_comparison_10x10.png", dpi=300, bbox_inches='tight')
plt.show()

# Print summary statistics
print("Summary Statistics for 10x10 Matrix:")
print("-" * 60)
print(f"{'k':<10} {'Java Time':<12} {'Python Time':<12} {'Time Ratio':<12} {'Java Calls':<12} {'Python Calls':<12} {'Calls Ratio':<12}")
print("-" * 60)

for i in range(len(java_data)):
    k = java_data.iloc[i]['k']
    java_time = java_data.iloc[i]['avg_time_sec']
    python_time = python_data.iloc[i]['avg_time_sec']
    java_calls = java_data.iloc[i]['avg_hungarian_calls']
    python_calls = python_data.iloc[i]['avg_hungarian_calls']
    
    print(f"{k:<10} {java_time:<12.4f} {python_time:<12.4f} {time_ratio[i]:<12.1f} {java_calls:<12.0f} {python_calls:<12.0f} {calls_ratio[i]:<12.1f}")

# Calculate and print growth rates
print("\nGrowth Analysis:")
print("-" * 40)
k_values = java_data['k'].values
k_growth = k_values[-1] / k_values[0]

java_time_growth = java_data['avg_time_sec'].values[-1] / java_data['avg_time_sec'].values[0]
python_time_growth = python_data['avg_time_sec'].values[-1] / python_data['avg_time_sec'].values[0]

java_calls_growth = java_data['avg_hungarian_calls'].values[-1] / java_data['avg_hungarian_calls'].values[0]
python_calls_growth = python_data['avg_hungarian_calls'].values[-1] / python_data['avg_hungarian_calls'].values[0]

print(f"k increased by: {k_growth:.1f}x ({k_values[0]:,} to {k_values[-1]:,})")
print(f"Java time increased by: {java_time_growth:.1f}x")
print(f"Python time increased by: {python_time_growth:.1f}x")
print(f"Java calls increased by: {java_calls_growth:.1f}x")
print(f"Python calls increased by: {python_calls_growth:.1f}x")

# Save comparison data to CSV for future reference
comparison_df = pd.DataFrame({
    'k': java_data['k'].values,
    'java_time': java_data['avg_time_sec'].values,
    'python_time': python_data['avg_time_sec'].values,
    'time_ratio': time_ratio,
    'java_calls': java_data['avg_hungarian_calls'].values,
    'python_calls': python_data['avg_hungarian_calls'].values,
    'calls_ratio': calls_ratio
})
comparison_df.to_csv('performance_comparison_10x10.csv', index=False)
print("\nComparison data saved to 'performance_comparison_10x10.csv'")