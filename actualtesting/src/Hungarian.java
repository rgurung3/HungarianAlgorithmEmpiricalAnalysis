import java.util.Arrays;

public class Hungarian {

    public static void printMatrix(int[][] costs, int[] rowPrices, int[] colPrices) {
        int size = costs.length;
        System.out.println("=====");
        for (int row = 1; row <= size; row++) {
            for (int col = 1; col <= size; col++) {
                int cost = costs[row-1][col-1] - rowPrices[row] - colPrices[col];
                System.out.print(" " + cost);
            }
            System.out.println();
        }
    }

    /**
     * @param costs The original cost matrix (n x n).
     * @return An array where result[i] = j means row i is assigned to column j.
     *
     * Note that arrays are 1-index for the algorithm.
     */
    public static int[] solveHungarian(int[][] costs) {
        int size = costs.length;
        int[][] costsCopy = deepCopy(costs);

        // for modifying costs
        int[] rowPrices = new int[size + 1];
        int[] colPrices = new int[size + 1];
        // for Dijkstra's algorithm
        int[] columnToRow = new int[size + 1];
        int[] columnToLastColumn = new int[size + 1];

        for (int row = 1; row <= size; row++) {
            // run Dijkstra's algorithm for the next row
            solveNext(row, costsCopy, rowPrices, colPrices, columnToRow, columnToLastColumn);
        }

        // return assignment as 0-indexed
        int[] assignment = new int[size];
        for (int column = 1; column <= size; column++) {
            assignment[columnToRow[column] - 1] = column - 1;
        }
        return assignment;
    }

    /**
     * @param initialRow The next row of the matrix to solve.
     * @param costs The input cost matrix.
     * @param rowPrices Row updates to cost matrix.
     * @param colPrices Column updates to cost matrix.
     * @param columnToRow Mapping from column to its matched row.
     * @param columnToLastColumn Mapping from column to the previous column used to reach it.
     */
    private static void solveNext(int initialRow, 
                                  int[][] costs, int[] rowPrices, int[] colPrices,
                                  int[] columnToRow, int[] columnToLastColumn) {

        int size = costs.length;

        // for Dijkstra's algorithm
        boolean[] visitedColumn = new boolean[size + 1];
        int[] distanceToColumn = new int[size + 1];
        Arrays.fill(distanceToColumn, Integer.MAX_VALUE);

        // the graph is a bipartite graph of row nodes and column nodes.
        // find shortest path from the initial row, to any unmatched column.
        int closestColumn = 0;                   // 0 is a dummy column
        columnToRow[closestColumn] = initialRow; // dummy matching to dummy column
        do {
            // start by visiting the column node
            visitedColumn[closestColumn] = true;
            // there is a unique outgoing edge, so immediately move to the row node
            // (this col->row edge was reweighted to have 0 cost)
            int row = columnToRow[closestColumn];

            // update the distances to columns by going through new row
            for (int column = 1; column <= size; column++) {
                if (!visitedColumn[column]) {
                    // reweighted cost
                    int cost = costs[row - 1][column - 1] - rowPrices[row] - colPrices[column];
                    if (cost < distanceToColumn[column]) {
                        distanceToColumn[column] = cost;
                        columnToLastColumn[column] = closestColumn;
                    }
                }
            }

            // check for the next closest unvisited column
            int minDistance = Integer.MAX_VALUE;
            for (int column = 1; column <= size; column++) {
                if (!visitedColumn[column]) {
                    if (distanceToColumn[column] < minDistance) {
                        minDistance = distanceToColumn[column];
                        closestColumn = column;
                    }
                }
            }

            // update the weights (prices).  note that updating the
            // row weights, or updating the column weights do not
            // change the optimal solution to the assignment problem
            for (int column = 0; column <= size; column++) {
                if (visitedColumn[column]) {
                    // this reduces the cost of all visited rows, with
                    // the closest row/col pair reduced to zero cost
                    rowPrices[columnToRow[column]] += minDistance;
                    // this offsets the reduced cost above, to keep
                    // the previous zero's non-negative
                    colPrices[column] -= minDistance;
                } else {
                    // all the row-to-column distances were updated,
                    // so distances vector should also be updated
                    distanceToColumn[column] -= minDistance;
                }
            }

            // at this point, the shortest path to the closest column
            // has a distance of zero from the starting row.  if the
            // closest column is also unmatched, then we are done.
        } while (columnToRow[closestColumn] != 0);
        // when the loop ends, we know the (partial) matching is
        // optimal since it has a cost of zero.

        updateMatching(closestColumn, columnToRow, columnToLastColumn);
    }

    /**
     * @param column Final column in the augmenting path.
     * @param columnToRow Map from column to the matching row.
     * @param columnToLastColumn Map from column to the last column on the augmenting path.
     */
    private static void updateMatching(int column, int[] columnToRow, int[] columnToLastColumn) {
        // columnToLastColumn has the sequence of columns on the
        // shortest path starting from the initial column 0, to the
        // unmatched column which is input.  This sequence of edges is 
        //    unmatched-matched-unmatched-...-unmatched
        // which we need to toggle.
        do {
            int lastColumn = columnToLastColumn[column];
            columnToRow[column] = columnToRow[lastColumn];
            column = lastColumn;
        } while (column != 0);
    }

    /**
     * Creates a deep copy of a 2D matrix.
     *
     * @param original The original matrix.
     * @return A deep copy of the original matrix.
     */
    private static int[][] deepCopy(int[][] original) {
        int[][] copy = new int[original.length][];
        for (int i = 0; i < original.length; i++)
            copy[i] = original[i].clone();
        return copy;
    }
}
