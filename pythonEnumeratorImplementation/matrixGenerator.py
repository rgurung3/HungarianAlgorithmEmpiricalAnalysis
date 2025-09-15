import numpy as np

np.random.seed(42)
n = 25
matrix = np.random.randint(1000, 10000, size=(n, n))

with open('cost_matrix_25.txt', 'w') as f:
    for row in matrix:
        f.write(' '.join(map(str, row)) + '\n')