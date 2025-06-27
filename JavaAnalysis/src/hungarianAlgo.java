import java.io.IOException;
import java.util.*;

public class hungarianAlgo {

    public static void main(String[] args) throws IOException {
        List<MatrixEntry> matrixEntries = MatrixEntry.loadMatrices("huge_benchmark_matrices.txt");
        for (MatrixEntry matrix : matrixEntries) {
            BenchMarks.run(matrix.name, matrix.matrix, 50);
        }
    }

    /**
     * @param costMatrix The original cost matrix (n x n).
     * @return An array where result[i] = j means row i is assigned to column j.
     *
     * - A deep copy of the matrix is made since the algorithm modifies cost values during computation, and we don't want to modify the original matrix, because the final cost is going to be calculated from that.
     * - 1-based indexing is used for convenience; mostly used to serve as a dummy placeholder during path construction and didn't want to mess around with switching indexes much.
     *
     * The method also initializes the following arrays:
     * -dualVariableForRows: The label for each row, which helps in defining the reduced cost.
     * -dualVariableForColumns: The label for each column.
     * -columnMatching: Stores which row each column is currently matched to.
     * -pathToColumn: Records the augmenting path so that the algorithm can reassign matches when a better path is found.
     *
     * For each row, it calls the executePhase() method to find a valid matching, and after all the rows are matched, it constructs and returns the result in row-major format.
     */
    public static int[] solveHungarian(int[][] costMatrix) {

        int matrixSize = costMatrix.length;
        int[][] workingCostMatrix = deepCopy(costMatrix);

        int[] dualVariablesForRows = new int[matrixSize + 1];
        int[] dualVariablesForColumns = new int[matrixSize + 1];
        int[] columnMatching = new int[matrixSize + 1];
        int[] pathToColumn = new int[matrixSize + 1];

        for (int currentRow = 1; currentRow <= matrixSize; currentRow++) {
            executePhase(currentRow, workingCostMatrix, dualVariablesForRows, dualVariablesForColumns, columnMatching, pathToColumn);
        }

        int[] resultAssignment = new int[matrixSize];
        for (int column = 1; column <= matrixSize; column++) {
            resultAssignment[columnMatching[column] - 1] = column - 1;
        }
        return resultAssignment;
    }

    /**
     * @param currentRow The current row being considered.
     * @param costMatrix The working cost matrix.
     * @param dualVariablesForRows Dual variables for rows.
     * @param dualVariablesForColumns Dual variables for columns.
     * @param columnMatching Current matching array.
     * @param pathToColumn Helper array for reconstructing the path.
     * This method tries to match the given 'currentRow' to a column by building an augmenting path ny using the minimum slack idea (The formula is the value of cost[i][j]-u[i]-v[j] where u and v are the row and columns being used).
     * If the row is already matched indirectly, the algorithm reroutes previous matches to make space.
     *
     * The method makes use of helpful variables such as:
     * -minimumSlack array: This stores the smallest reduced cost seen for each column so far.
     * -visitedColumns: This keeps track of which columns have already been visited during this phase, because if the column is already visited then we do not want to consider that column anymore.
     *
     *  The method mostly uses variables that are passed to it from the solveHungarian() method, so other variables are explained in the method above.
     *
     *  As the scan progresses, the dual variables are updated using the smallest slack found so far, allowing new edges to appear where the reduced cost becomes zero. These edges are also called tight edges, meaning that they are
     *  currently the most optimal assignment based on the adjusted costs. Once the algorithm reaches an unmatched column (This basically means that the column is not yet assigned to any row), it stops and calls reconstructPath() to finalize
     *  the assignment by retracting the path (or just that the valid augmenting path has been found, and we can update the matrix).
     */
    private static void executePhase(int currentRow, int[][] costMatrix,
                                     int[] dualVariablesForRows, int[] dualVariablesForColumns,
                                     int[] columnMatching, int[] pathToColumn) {

        int matrixSize = costMatrix.length;
        columnMatching[0] = currentRow;

        int currentColumn = 0;
        int[] minimumSlack = new int[matrixSize + 1];
        boolean[] visitedColumns = new boolean[matrixSize + 1];
        Arrays.fill(minimumSlack, Integer.MAX_VALUE);

        do {
            visitedColumns[currentColumn] = true;
            int rowBeingScanned = columnMatching[currentColumn];
            int smallestSlack = Integer.MAX_VALUE;
            int columnWithSmallestSlack = -1;

            for (int candidateColumn = 1; candidateColumn <= matrixSize; candidateColumn++) {
                if (!visitedColumns[candidateColumn]) {
                    int slack = costMatrix[rowBeingScanned - 1][candidateColumn - 1]
                            - dualVariablesForRows[rowBeingScanned]
                            - dualVariablesForColumns[candidateColumn];
                    if (slack < minimumSlack[candidateColumn]) {
                        minimumSlack[candidateColumn] = slack;
                        pathToColumn[candidateColumn] = currentColumn;
                    }
                    if (minimumSlack[candidateColumn] < smallestSlack) {
                        smallestSlack = minimumSlack[candidateColumn];
                        columnWithSmallestSlack = candidateColumn;
                    }
                }
            }

            for (int column = 0; column <= matrixSize; column++) {
                if (visitedColumns[column]) {
                    dualVariablesForRows[columnMatching[column]] += smallestSlack;
                    dualVariablesForColumns[column] -= smallestSlack;
                } else {
                    minimumSlack[column] -= smallestSlack;
                }
            }

            currentColumn = columnWithSmallestSlack;
        } while (columnMatching[currentColumn] != 0);

        reconstructPath(currentColumn, columnMatching, pathToColumn);
    }

    /**
     * @param currentColumn Final column in the augmenting path.
     * @param columnMatching Matching array (column to row).
     * @param pathToColumn Helper array to backtrack the path.
     *
     * Starting from the unmatched column (the end of the augmenting path), this method traces backward through the 'pathToColumn' array, using each recorded predecessor to step through the path in reverse.
     *
     * At each step, the columnMatching array is updated to assign the column to the row that led to it. This process updates all the matches along the path that was found, thus making room for the new
     * match at the end.
     *
     * This sets up the new assignment found in executePhase() and ensure that the matching stays valid and up to date. Basically, after this method executes, the row that was unmatched let's say is now
     * successfully assigned to a column.
     */
    private static void reconstructPath(int currentColumn, int[] columnMatching, int[] pathToColumn) {
        do {
            int previousColumn = pathToColumn[currentColumn];
            columnMatching[currentColumn] = columnMatching[previousColumn];
            currentColumn = previousColumn;
        } while (currentColumn != 0);
    }

    /**
     * Creates a deep copy of a 2D matrix.
     *
     * @param original The original matrix.
     * @return A deep copy of the original matrix.
     */
    private static int[][] deepCopy(int[][] original) {
        int[][] copy = new int[original.length][original[0].length];
        for (int i = 0; i < original.length; i++)
            copy[i] = original[i].clone();
        return copy;
    }
}
