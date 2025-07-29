import java.util.*;

public class testImplementation {

    static String[] GT_labels = {"A", "B", "C"};
    static String[] Pred_labels = {"1", "2", "3"};
    static String[] D_GT_labels = {"D1", "D2", "D3"};
    static String[] D_Pred_labels = {"DA", "DB", "DC"};

    public static void main(String[] args) {
        runCase(1, new int[][] {
            {1, 5, 9},
            {6, 2, 8},
            {7, 4, 3}
        });

        runCase(2, new int[][] {
            {1, 5, 100},
            {6, 2, 100}
        });

        runCase(3, new int[][] {
            {1, 5, 9},
            {6, 2, 8},
            {40, 50, 100}
        });

        runCase(4, new int[][] {
            {1, 5, 9},
            {6, 2, 8},
            {100, 120, 89}
        });

        runCase(5, new int[][] {
            {1, 5, 9},
            {6, 2, 8},
            {15, 8, 11}
        });

        runCase(6, new int[][] {
            {1, 5, 9},
            {6, 2, 8},
            {15, 18, 21}
        });
    }

    public static void runCase(int caseNum, int[][] costMatrix) {
        System.out.println("=== Case " + caseNum + " ===");

        MatrixPreProcessor mp = new MatrixPreProcessor();
        AssignmentBreakdown result = mp.runHungarianAndClassify(costMatrix);

        System.out.println("True Matches:");
        for (int[] match : result.trueMatches) {
            String gt = getSafe(GT_labels, match[0]);
            String pred = getSafe(Pred_labels, match[1]);
            System.out.println(gt + " -> " + pred);
        }

        System.out.println("\nFalse Positives (Predicted matched to dummy GT):");
        for (int[] pair : result.falsePositives) {
            String pred = getSafe(Pred_labels, pair[0]);
            String dummyGT = getSafe(D_GT_labels, pair[1]);
            System.out.println(pred + " -> " + dummyGT);
        }

        System.out.println("\nFalse Negatives (GT matched to dummy Prediction):");
        for (int[] pair : result.falseNegatives) {
            String gt = getSafe(GT_labels, pair[0]);
            String dummyPred = getSafe(D_Pred_labels, pair[1]);
            System.out.println(gt + " -> " + dummyPred);
        }

        System.out.println();
    }

    private static String getSafe(String[] arr, int idx) {
        return idx >= 0 && idx < arr.length ? arr[idx] : "?" + idx;
    }
}
