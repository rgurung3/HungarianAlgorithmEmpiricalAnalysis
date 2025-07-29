import java.util.*;

public class testImplementation {
    public static void main(String[] args) {
        int[][] costMatrix = {
            {1, 5, 9},
            {6, 2, 8},
            {15, 18, 21}
        };

        AssignmentBreakdown result = MatrixPreProcessor.runHungarianAndClassify(costMatrix);

        System.out.println("True Matches:");
        for (int[] match : result.trueMatches) {
            System.out.println("GT_" + (match[0]) + "-> Pred_" + match[1]);
        }

        System.out.println("\nFalse Positives (Predicted matched to dummy GT):");
        for (int[] pair : result.falsePositives) {
            System.out.println("Pred_" + pair[0] + " → D_GT_" + pair[1]);
        }

        System.out.println("\nFalse Negatives (GT matched to dummy Prediction):");
        for (int[] pair : result.falseNegatives) {
            System.out.println("GT_" + pair[0] + " → D_Pred_" + pair[1]);
        }
    }
}
