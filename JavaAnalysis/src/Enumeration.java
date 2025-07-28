import java.util.*;

public class Enumeration {

    /*
     * As discussed for part 2A over here.
     */
    public static List<AssignmentResult> generateAllAssignments(int[][] costMatrix) {
        List<Integer> initialAssignment = new ArrayList<>();
        int numTasks = costMatrix.length;
        for (int col = 0; col < numTasks; col++) {
            initialAssignment.add(col);
        }

        List<AssignmentResult> allResults = new ArrayList<>();
        generatePermutations(initialAssignment, 0, costMatrix, allResults);

        allResults.sort(Comparator.comparingDouble(a -> a.totalCost));
        return allResults;
    }

    private static void generatePermutations(List<Integer> currentAssignment, int rowIndex,
                                             int[][] costMatrix, List<AssignmentResult> results) {
        int numTasks = costMatrix.length;
        if (rowIndex == numTasks) {
            double totalCost = 0;
            for (int row = 0; row < numTasks; row++) {
                int col = currentAssignment.get(row);
                totalCost += costMatrix[row][col];
            }
            results.add(new AssignmentResult(new ArrayList<>(currentAssignment), totalCost));
        } else {
            for (int i = rowIndex; i < numTasks; i++) {
                Collections.swap(currentAssignment, rowIndex, i);
                generatePermutations(currentAssignment, rowIndex + 1, costMatrix, results);
                Collections.swap(currentAssignment, rowIndex, i);
            }
        }
    }

    public static boolean isValid(List<Integer> assignments, List<int[]> exclusions, List<int[]> inclusions) {
        for (int i = 0; i < assignments.size(); i++) {
            for (int[] val : exclusions) {
                if (val[0] == i && val[1] == assignments.get(i)) {
                    return false;
                }
            }

            for (int[] val : inclusions) {
                if (assignments.get(val[0]) != val[1]) {
                    return false;
                }
            }
        }
        return true;
    }

    public static AssignmentResult getBestValidAssignment(List<AssignmentResult> results, List<int[]> exclusions, List<int[]> inclusions) {
        for (AssignmentResult result : results) {
            if (isValid(result.assignments, exclusions, inclusions)) {
                return result;
            }
        }
        return null;
    }

    /*
     * part 2B begins here.
     */
    /**
     * 
     * @param matrix
     * @param exclusions: A list of the pairs of exclusions to be excluded out from the matrix.
     * @return the cost matrix after the excluded pairs have been changed to maximums.
     * 
     */
    public static int[][] finalCostMatrixAfterExclusionsAndInclusions(int[][] matrix, List<int[]> exclusions, List<int[]> inclusions, boolean includeOrNot) {
        int n = matrix.length;
        int replacingValue = totalSum(matrix) + 1;
        int blockingValue = replacingValue + 1;
        int[][] workingMatrix = deepCopy(matrix);

        if (includeOrNot) {
            for (int[] pair : inclusions) {
                int row = pair[0];
                int col = pair[1];

                workingMatrix[row][col] = 0;

                // Block all other cells in this row
                for (int j = 0; j < n; j++) {
                    if (j != col) {
                        workingMatrix[row][j] = blockingValue;
                    }
                }

                // Block all other cells in this column
                for (int i = 0; i < n; i++) {
                    if (i != row) {
                        workingMatrix[i][col] = blockingValue;
                    }
                }
            }
        }

        for (int[] pair : exclusions) {
            int row = pair[0];
            int col = pair[1];

            // Check if this cell is part of an inclusion
            boolean isIncluded = false;
            for (int[] inc : inclusions) {
                if (inc[0] == row && inc[1] == col) {
                    isIncluded = true;
                    break;
                }
            }

            // Only apply exclusion if it's not an included cell
            if (!isIncluded) {
                workingMatrix[row][col] = replacingValue;
            }
        }

        return workingMatrix;
    }

    private static int[][] deepCopy(int[][] original) {
        int[][] copy = new int[original.length][original[0].length];
        for (int i = 0; i < original.length; i++)
            copy[i] = original[i].clone();
        return copy;
    }

    /**
     * 
     * @param results
     * Method to print out the results.
     */
    public static void printResults(List<AssignmentResult> results) {
        int count = 0;
        for (AssignmentResult result : results) {
            count++;
            System.out.println(result.assignments + " and the total cost of it is: " + result.totalCost);
        }
        System.out.println("The total amount of results: " + count);
    }

    public static int totalSum(int[][] matrix) {
        int totalSum = 0;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                totalSum += matrix[i][j];
            }
        }
        return totalSum;
    }

    public static List<int[]> exclusionsListFromHungarian(int[] assignments) {
        List<int[]> returningList = new ArrayList<>();
        for (int i = 0; i < assignments.length; i++) {
            returningList.add(new int[]{i, assignments[i]});
        }
        return returningList;
    }

    /*
     * Method to get the top k paths.
     */
    public static List<AssignmentResult> getTopKMurtys(int[][] costMatrix, int k) {
        List<AssignmentResult> results = new ArrayList<>();
        PriorityQueue<MurtyNode> pq = new PriorityQueue<>();

        int[] baseAssign = hungarianAlgo.solveHungarian(costMatrix);
        if (baseAssign == null) return results;

        List<Integer> baseList = toList(baseAssign);
        double baseCost = calculateCost(costMatrix, baseAssign);
        AssignmentResult baseResult = new AssignmentResult(baseList, baseCost);

        pq.offer(new MurtyNode(baseResult, new ArrayList<>(), new ArrayList<>()));

        int infeasibleThreshold = totalSum(costMatrix) + 1;

        while (!pq.isEmpty() && results.size() < k) {
            MurtyNode current = pq.poll();
            results.add(current.result);

            List<Integer> currentAssignment = current.result.assignments;
            int n = currentAssignment.size();

            // Find the first position that's not already forced by inclusions
            int startPos = 0;
            for (int[] inc : current.inclusions) {
                startPos = Math.max(startPos, inc[0] + 1);
            }

            for (int i = startPos; i < n; i++) {
                // Build new inclusions: force all assignments from startPos to i-1
                List<int[]> newInclusions = new ArrayList<>(current.inclusions);
                for (int j = startPos; j < i; j++) {
                    newInclusions.add(new int[]{j, currentAssignment.get(j)});
                }

                // Add exclusion at position i
                List<int[]> newExclusions = new ArrayList<>(current.exclusions);
                newExclusions.add(new int[]{i, currentAssignment.get(i)});

                // Modify cost matrix
                int[][] modifiedMatrix = finalCostMatrixAfterExclusionsAndInclusions(
                        costMatrix, newExclusions, newInclusions, true
                );

                // Solve subproblem
                int[] newAssignment = hungarianAlgo.solveHungarian(modifiedMatrix);
                if (newAssignment == null) continue;

                // Check if the solution is infeasible based on modified cost
                double modCost = calculateCost(modifiedMatrix, newAssignment);
                if (modCost >= infeasibleThreshold) {
                    continue;
                }

                // Calculate actual cost and add to queue
                double actualCost = calculateCost(costMatrix, newAssignment);
                AssignmentResult newResult = new AssignmentResult(toList(newAssignment), actualCost);
                pq.offer(new MurtyNode(newResult, newExclusions, newInclusions));
            }
        }

        return results;
    }

    public static boolean compareMethods(List<AssignmentResult> murty, List<AssignmentResult> bruteForce) {
        if (murty.size() != bruteForce.size()) {
            System.out.println("Different number of solutions!");
            System.out.println("Murty: " + murty.size() + ", Brute Force: " + bruteForce.size());
            return false;
        }

        boolean allMatch = true;
        for (int i = 0; i < murty.size(); i++) {
            double murtysCost = murty.get(i).totalCost;
            double bruteForceCost = bruteForce.get(i).totalCost;

            if (Math.abs(murtysCost - bruteForceCost) > 0.001) {
                System.out.println("Cost mismatch at position " + i);
                System.out.println("Murty: " + murty.get(i).assignments + " cost=" + murtysCost);
                System.out.println("Brute: " + bruteForce.get(i).assignments + " cost=" + bruteForceCost);
                allMatch = false;
            }
        }

        if (allMatch) {
            System.out.println("All results match between Murty and Brute Force");
        }

        return allMatch;
    }

    public static int[][] generateMatrix(int size, int min, int max) {
        Random rand = new Random();
        int[][] returnMatrix = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                returnMatrix[i][j] = rand.nextInt(max - min + 1) + min;
            }
        }
        return returnMatrix;
    }

    public static void printMatrix(int[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            System.out.print("[");
            for (int j = 0; j < matrix[0].length; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.print("\b]");
            System.out.println();
        }
    }

    public static List<Integer> toList(int[] array) {
        List<Integer> list = new ArrayList<>();
        for (int num : array) {
            list.add(num);
        }
        return list;
    }

    public static double calculateCost(int[][] matrix, int[] assignment) {
        double cost = 0;
        for (int i = 0; i < assignment.length; i++) {
            cost += matrix[i][assignment[i]];
        }
        return cost;
    }

    public static String toStringList(List<int[]> list) {
        StringBuilder sb = new StringBuilder();
        for (int[] arr : list) {
            sb.append("(").append(arr[0]).append(",").append(arr[1]).append(") ");
        }
        return sb.toString().trim();
    }
}
