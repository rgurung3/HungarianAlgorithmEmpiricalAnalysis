import java.util.*;

public class testImplementation{
    public static void main(String[] args) {
        int D = 10;
        int[][] matrix = {
            {  1,   5,   9,  D,   D,   D }, 
            {  6,   2,   8,  D,   D,   D },
            {  33,   45,   21,  D,   D,   D},
            {  D,   D,   D,  0,   0,   0 },
            {  D,   D,   D,  0,   0,   0 }, 
            {  D,   D,   D,  0,   0,   0 } 
        };

        // Step 3: Print expanded matrix
        System.out.println("Cost Matrix:");
        for (int[] row : matrix) {
            System.out.println(Arrays.toString(row));
        }

        hungarianAlgo hungarian = new hungarianAlgo();
        int[] assignment = hungarian.solveHungarian(matrix);

        System.out.println("\nAssignments:");
        double totalCost = 0;
        for (int i = 0; i < assignment.length; i++) {
            System.out.printf("Row %d → Col %d | Cost: %d%n", i, assignment[i], matrix[i][assignment[i]]);
            totalCost += matrix[i][assignment[i]];
        }
        System.out.println("Total Cost: " + totalCost);
    }
}