import java.util.List;

public class Test {

    static String[] GT_labels = {"A", "B", "C"};         // ground truth
    static String[] Pred_labels = {"1", "2", "3"};       // prediction
    static String[] D_GT_labels = {"1*", "2*", "3*"};    // dummy
    static String[] D_Pred_labels = {"A*", "B*", "C*"};  // dummy

    private static String groundTruthLabel(int i) {
        char letter = (char)((int)'A' + i);
        return Character.toString(letter);
    }

    private static String predictionLabel(int i) {
        return Integer.toString(i+1);
    }

    private static String dummyGroundTruthLabel(int i) {
        return groundTruthLabel(i)  + "*";
    }

    private static String dummyPredictionLabel(int i) {
        return predictionLabel(i)  + "*";
    }

    public static void main(String[] args) {
        runHungarianTest(1, new int[][] {
            {1, 5, 9},
            {6, 2, 8},
            {7, 4, 3}
        });

        runHungarianTest(2, new int[][] {
            {1, 5, 100},
            {6, 2, 100}
        });

        runHungarianTest(3, new int[][] {
            {1, 5, 9},
            {6, 2, 8},
            {40, 50, 100}
        });

        runHungarianTest(4, new int[][] {
            {1, 5, 9},
            {6, 2, 8},
            {100, 120, 89}
        });

        runHungarianTest(5, new int[][] {
            {1, 5, 9},
            {6, 2, 8},
            {15, 8, 11}
        });

        runHungarianTest(6, new int[][] {
            {1, 5, 9},
            {6, 2, 8},
            {15, 18, 21}
        });

        runHungarianTest(6, new int[][] {
            {1, 5, 9},
            {6, 2, 8},
            {15, 18, 21}
        });

        runHungarianTest(101, new int[][] {
            {13, 8, 19},
            {7, 18, 13},
            {20, 22, 5}
        });

        runHungarianTest(102, new int[][] {
            {4, 14},
            {18, 20},
            {13, 3}
        });

        runHungarianTest(103, new int[][] {
            {8, 12, 16},
            {17, 5, 8}
        });

        runHungarianTest(104, new int[][] {
            {5, 34, 38},
            {22, 21, 8},
            {32, 20, 6}
        });
        
        runEnumerationTest(201, new int[][] {
            {13, 15, 19, 14},
            {12, 18, 13, 14},
            {10, 14, 8,  16}
        });

        runEnumerationTest(202, new int[][] {
            {13, 15, 19},
            {12, 18, 13},
            {10, 14, 8 },
            {20, 22, 18}
        });

        runEnumerationTest(203, new int[][] {
            {13, 15, 19, 14},
            {12, 18, 13, 14},
            {10, 14, 8,  16},
            {20, 22, 18, 16}
        });

    }

    /**
     * Examples for running the Hungarian algorithm with false
     * positives and false negatives.
     *
     */
    public static void runHungarianTest(int caseNum, int[][] costMatrix) {
        System.out.println("=== Test " + caseNum + " ===");
        // specify fp and fn rates
        int falsePositiveCost = 10;
        int falseNegativeCost = 10;

        // create problem instance
        AssignmentProblem baseProblem = new AssignmentProblem(costMatrix,
                                        falsePositiveCost,falseNegativeCost);
        // augment with false positives and negatives
        int[][] matrix = baseProblem.costMatrixWithFailures();
        AssignmentProblem problem = new AssignmentProblem(matrix);
        // create algorithm instance
        Hungarian algorithm = new Hungarian(problem);
        AssignmentSolution baseSolution = algorithm.solve();

        // print problem
        System.out.println("Cost Matrix:");
        for (int[] row : matrix) {
            System.out.println(java.util.Arrays.toString(row));
        }

        // extract false positives and negatives from augmented solution
        AssignmentSolution solution = baseSolution.extractFailures(baseProblem);

        // print solution
        System.out.println("Assignment:");
        System.out.println(solution.assignment);

        System.out.println("True Matches:");
        for (int[] pair : solution.matches) {
            String st1 = groundTruthLabel(pair[0]);
            String st2 = predictionLabel(pair[1]);
            System.out.println(st1 + " -> " + st2);
        }

        System.out.println("False Positives:");
        for (int[] pair : solution.falsePositives) {
            String st1 = predictionLabel(pair[1]);
            String st2 = dummyPredictionLabel(pair[0]);
            System.out.println(st1 + " -> " + st2);
        }

        System.out.println("False Negatives:");
        for (int[] pair : solution.falseNegatives) {
            String st1 = groundTruthLabel(pair[0]);
            String st2 = dummyGroundTruthLabel(pair[1]);
            System.out.println(st1 + " -> " + st2);
        }

        System.out.println();
    }

    /**
     * Examples for running the k-best solutions with false positives
     * and false negatives.
     *
     */
    public static void runEnumerationTest(int caseNum, int[][] costMatrix) {
        System.out.println("=== Test " + caseNum + " ===");
        // specify fp and fn rates
        int falsePositiveCost = 10;
        int falseNegativeCost = 10;
        // specify # of solutions to enumerate
        int k = 10;

        // create base problem instance
        AssignmentProblem baseProblem = new AssignmentProblem(costMatrix,
                                        falsePositiveCost,falseNegativeCost);
        // augment with false positives and negatives
        int[][] matrix = baseProblem.costMatrixWithFailures();
        AssignmentProblem problem = new AssignmentProblem(matrix);
        // create algorithm instance
        MurtyEnumerator enumerator = new MurtyEnumerator(problem);
        List<AssignmentSolution> solutions = enumerator.enumerate(k);

        // print problem
        System.out.println("Cost Matrix:");
        for (int[] row : matrix) {
            System.out.println(java.util.Arrays.toString(row));
        }

        for (int i = 0; i < k; i++) {
            // get next solution
            AssignmentSolution solution = solutions.get(i);

            // extract false positives and negatives from augmented solution
            solution = solution.extractFailures(baseProblem);

            // print solution
            System.out.printf("Assignment %d (cost %d):", i, solution.cost);
            System.out.print(" " + solution.assignment);

            System.out.print(" True: ");
            for (int[] pair : solution.matches) {
                String st1 = groundTruthLabel(pair[0]);
                String st2 = predictionLabel(pair[1]);
                System.out.print(st1 + " -> " + st2 + " ");
            }

            System.out.print("FP: ");
            for (int[] pair : solution.falsePositives) {
                String st1 = predictionLabel(pair[1]);
                String st2 = dummyPredictionLabel(pair[0]);
                System.out.print(st1 + " -> " + st2 + " ");
            }

            System.out.print("FN: ");
            for (int[] pair : solution.falseNegatives) {
                String st1 = groundTruthLabel(pair[0]);
                String st2 = dummyGroundTruthLabel(pair[1]);
                System.out.print(st1 + " -> " + st2 + " ");
            }
            System.out.println();
        }
        System.out.println();
    }
}
