import java.util.*;

public class MatrixPreProcessor {

    public  AssignmentBreakdown runHungarianAndClassify(int[][] originalMatrix) {
        int m = originalMatrix.length;
        int n = originalMatrix[0].length;
        int size = m + n;
//        int dummyCost = totalSum(originalMatrix);

        int[][] expanded = addDummies(originalMatrix, 10);

        System.out.println("Expanded Cost Matrix:");
        for (int[] row : expanded) {
            System.out.println(Arrays.toString(row));
        }
        System.out.println("Running hungarian here.");
        int[] assignment = hungarianAlgo.solveHungarian(expanded);

        AssignmentBreakdown result = new AssignmentBreakdown();

        for (int i = 0; i < size; i++) {
            int j = assignment[i];

            if (i < m && j < n) {
                // GT_i matched to Pred_j
                result.trueMatches.add(new int[]{i, j});
            }
            else if (i < m && j >= n) {
                // GT_i matched to dummy prediction → FN
                result.falseNegatives.add(new int[]{i, j - n});
            }
            else if (i >= m && j < n) {
                // Pred_j matched to dummy GT -> FP
                result.falsePositives.add(new int[]{j, i - m});
            }
           
        }

        return result;
    }

    public int[][] addDummies(int[][] matrix, int dummyCost) {
        int m = matrix.length;         
        int n = matrix[0].length; 
        int size = m + n;
        int[][] newMatrix = new int[size][size];
        int INF = totalSum(matrix) + 1;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i < m && j < n) {
                    // Top-left: original cost matrix
                    newMatrix[i][j] = matrix[i][j];
                } else if (i < m && j >= n) {
                    // Top-right: GT_i to dummy predicted tracks
                    newMatrix[i][j] = (j - n == i) ? dummyCost : INF;
                } else if (i >= m && j < n) {
                    // Bottom-left: dummy GTs to predicted tracks
                    newMatrix[i][j] = (i - m == j) ? dummyCost : INF;
                } else {
                    // Bottom-right: dummy-to-dummy
                    newMatrix[i][j] = 0;
                }
            }
        }

        return newMatrix;
    }
    public  int totalSum(int[][] matrix) {
        int sum = 0;
        for (int[] row : matrix) {
            for (int val : row) {
                sum += val;
            }
        }
        return sum + 1;
    }
}
