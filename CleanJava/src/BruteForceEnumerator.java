import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BruteForceEnumerator {

    public static List<AssignmentSolution> generateAllAssignments(int[][] costMatrix) {
        int numTasks = costMatrix.length;
        int[] initialAssignment = new int[numTasks];
        for (int col = 0; col < numTasks; col++)
            initialAssignment[col] = col;

        List<AssignmentSolution> allSolutions = new ArrayList<>();
        generatePermutations(initialAssignment, 0, costMatrix, allSolutions);

        allSolutions.sort(Comparator.comparingInt(a -> a.cost));
        return allSolutions;
    }

    private static void swap(int[] array, int i, int j) {
        int tmp = array[i];
        array[i] = array[j];
        array[j] = tmp;
    }

    private static void generatePermutations(int[] currentAssignment, int rowIndex,
                                             int[][] costMatrix, List<AssignmentSolution> solutions) {
        int numTasks = costMatrix.length;
        if (rowIndex == numTasks) {
            int totalCost = 0;
            for (int row = 0; row < numTasks; row++) {
                int col = currentAssignment[row];
                totalCost += costMatrix[row][col];
            }
            solutions.add(new AssignmentSolution(currentAssignment, totalCost));
        } else {
            for (int i = rowIndex; i < numTasks; i++) {
                swap(currentAssignment, rowIndex, i);
                generatePermutations(currentAssignment, rowIndex + 1, costMatrix, solutions);
                swap(currentAssignment, rowIndex, i);
            }
        }
    }

}
