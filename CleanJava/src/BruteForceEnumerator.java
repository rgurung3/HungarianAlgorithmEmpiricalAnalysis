import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BruteForceEnumerator {

    public static List<AssignmentSolution> generateAllAssignments(int[][] costMatrix) {
        List<Integer> initialAssignment = new ArrayList<>();
        int numTasks = costMatrix.length;
        for (int col = 0; col < numTasks; col++) {
            initialAssignment.add(col);
        }

        List<AssignmentSolution> allSolutions = new ArrayList<>();
        generatePermutations(initialAssignment, 0, costMatrix, allSolutions);

        allSolutions.sort(Comparator.comparingInt(a -> a.cost));
        return allSolutions;
    }

    private static void generatePermutations(List<Integer> currentAssignment, int rowIndex,
                                             int[][] costMatrix, List<AssignmentSolution> solutions) {
        int numTasks = costMatrix.length;
        if (rowIndex == numTasks) {
            int totalCost = 0;
            for (int row = 0; row < numTasks; row++) {
                int col = currentAssignment.get(row);
                totalCost += costMatrix[row][col];
            }
            solutions.add(new AssignmentSolution(new ArrayList<>(currentAssignment), totalCost));
        } else {
            for (int i = rowIndex; i < numTasks; i++) {
                Collections.swap(currentAssignment, rowIndex, i);
                generatePermutations(currentAssignment, rowIndex + 1, costMatrix, solutions);
                Collections.swap(currentAssignment, rowIndex, i);
            }
        }
    }

}
