import numpy as np

n = 20
num_matrices = 1

for seed in range(num_matrices):
    np.random.seed(seed)
    matrix = np.random.randint(1000,10000, size=(n, n))
    filename = f"cost_matrix_20.txt"
    np.savetxt(filename, matrix, fmt="%d")
    print(f"Saved {filename}")
