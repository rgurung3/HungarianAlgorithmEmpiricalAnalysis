import java.util.List;

public class AssignmentProblem {
    int[][] costMatrix;
    int numRows;
    int numCols; 
    int infinity;

    /**
     * @param costMatrix The cost matrix of an assignment problem.
     */
    public AssignmentProblem(int[][] costMatrix) {
        this.costMatrix = costMatrix;
        this.numRows = costMatrix.length;
        this.numCols = costMatrix[0].length;
        this.infinity = totalSum(costMatrix) + 1;
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


}
