import java.util.List;

/**
 * This class defines an Assignment Problem.  It is given as input to
 * the Assignment algorithms (Hungarian algorithm and top-k assignment
 * enumerators).
 *
 */
public class AssignmentProblem {
    int[][] costMatrix;
    int numRows;
    int numCols; 
    int infinity;

    boolean modelFailures;
    int falsePositiveCost;
    int falseNegativeCost;

    /**
     * @param costMatrix The cost matrix of an assignment problem.
     */
    public AssignmentProblem(int[][] costMatrix) {
        this.costMatrix = costMatrix;
        this.numRows = costMatrix.length;
        this.numCols = costMatrix[0].length;
        // infinity is the sum of all costs plus one
        this.infinity = totalSum(costMatrix) + 1;

        this.modelFailures = false;
        this.falsePositiveCost = this.infinity;
        this.falseNegativeCost = this.infinity;
    }

    /**
     * @param costMatrix The cost matrix of an assignment problem.
     * @param falsePositiveCost The cost for false positives (detecting noise).
     * @parame falseNegativeCost The cost for false negatives (missed detection).
     */
    public AssignmentProblem(int[][] costMatrix, 
                             int falsePositiveCost, int falseNegativeCost) {
        this.costMatrix = costMatrix;
        this.numRows = costMatrix.length;
        this.numCols = costMatrix[0].length;
        // infinity is the sum of all costs plus one
        this.infinity = totalSum(costMatrix) + 1;

        this.modelFailures = true;
        this.falsePositiveCost = falsePositiveCost;
        this.falseNegativeCost = falseNegativeCost;
        // we also add the additional costs from the false positive/negatives
        this.infinity += (falseNegativeCost)*numRows;
        this.infinity += (falsePositiveCost)*numCols;
    }

    /**
     * @param matrix cost matrix
     * @param assignment (partial) assignment (of the first r rows)
     * @return Cost of a (partial) assignment
     */
    public static int cost(int[][] matrix, int[] assignment) {
        int cost = 0;
        for (int i = 0; i < assignment.length; i++)
            cost += matrix[i][assignment[i]];
        return cost;
    }

    /**
     * @param matrix cost matrix
     * @param assignment (partial) assignment (of the first r rows)
     * @return Cost of a (partial) assignment
     */
    public static int cost(int[][] matrix, List<Integer> assignment) {
        int cost = 0;
        for (int i = 0; i < assignment.size(); i++)
            cost += matrix[i][assignment.get(i)];
        return cost;
    }

    /**
     * @param assignment (partial) assignment (of the first r rows)
     * @return Cost of a (partial) assignment
     */
    public int cost(int[] assignment) {
        return AssignmentProblem.cost(this.costMatrix,assignment);
    }

    /**
     * @param assignment (partial) assignment (of the first r rows)
     * @return Cost of a (partial) assignment
     */
    public int cost(List<Integer> assignment) {
        return AssignmentProblem.cost(this.costMatrix,assignment);
    }


    /**
     * Returns the sum of all entries in the cost matrix.  This number
     * plus one can be used as an "infinity" cost, to exlcude a
     * (row,column) pair from being an opimal solution to the
     * assignment problem.
     */
    int totalSum(int[][] matrix) {
        int totalSum = 0;
        for (int i = 0; i < this.numRows; i++) {
            for (int j = 0; j < this.numCols; j++) {
                totalSum += matrix[i][j];
            }
        }
        return totalSum;
    }

    /**
     * Augments the cost matrix to model false positives and false
     * negatives.  We interpret the rows as ground truth, and columns
     * as predictions.  New columns are added that represent missed
     * predictions (upper-right quadrant, of false negatives).  New
     * rows are added that represent noise (lower-left quadrant, or
     * false positives).
     *
     */
    protected int[][] costMatrixWithFailures() {
        if (this.modelFailures == false)
            return this.costMatrix;

        int newSize = this.numRows + this.numCols;
        int[][] newMatrix = new int[newSize][newSize];

        for (int i = 0; i < newSize; i++) {
            for (int j = 0; j < newSize; j++) {
                if (i < this.numRows && j < this.numCols) {
                    // upper-left quadrant: original cost matrix
                    newMatrix[i][j] = this.costMatrix[i][j];
                } else if (i < this.numRows && j >= this.numCols) {
                    // upper-right quadrant: false negatives
                    newMatrix[i][j] = (j - this.numCols == i) ? 
                        this.falseNegativeCost : this.infinity;
                } else if (i >= this.numRows && j < this.numCols) {
                    // lower-left quadrant: false positives
                    newMatrix[i][j] = (i - this.numRows == j) ? 
                        this.falsePositiveCost : this.infinity;
                } else {
                    // lower-right quadrant: dummy-to-dummy matchings
                    newMatrix[i][j] = 0;
                }
            }
        }

        return newMatrix;
    }
}
