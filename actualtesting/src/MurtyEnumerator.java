import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class MurtyEnumerator {
    AssignmentProblem problem;

    // stats for algorithm performance
    int cacheHits;
    int cacheMisses;
    public int totalCalls;
    public long totalTime;

    private int[][] baseMatrix;

    /**
     * Constructor for MurtyEnumerator
     * @param costMatrix of the assignment problem to enumerate
     */
    public MurtyEnumerator(AssignmentProblem problem) {
        this.problem = problem;
    }

    /**
     * Enumerates the top-k solutions to the assignment problem.
     * @param k The number of solutions to enumerate.
     * @return A list of solutions to the assignment problem.
     */
    public List<AssignmentResult> enumerate(int k) {
        // initialize data structures
        List<AssignmentResult> topK = new ArrayList<>();
        PriorityQueue<MurtyNode> pq = new PriorityQueue<>();
        int[][] costMatrix = this.problem.costMatrix;
        int n = costMatrix.length;
        int INF = this.problem.infinity;

        if(this.baseMatrix == null || this.baseMatrix.length !=n) {
            this.baseMatrix = new int[n][n];
        }
        final int[][] work = new int[n][n];

        // initial solution
        AssignmentResult baseResult = callHungarian(costMatrix);

        // initial node
        List<int[]> exclusions = new ArrayList<>();
        List<int[]> inclusions = new ArrayList<>();
        MurtyNode node = new MurtyNode(baseResult, exclusions, inclusions);
        pq.offer(node);

        while (topK.size() < k && !pq.isEmpty()) {
            // pop best solution
            node = pq.poll();
            topK.add(node.solution);

            List<Integer> currentAssignment = node.solution.assignment;
            int nAssign = currentAssignment.size();

            // Find the first position that's not already forced by inclusions
            int startPos = 0;
            for (int[] inc : node.inclusions) {
                startPos = Math.max(startPos, inc[0] + 1);
            }

            for (int i = startPos; i < nAssign; i++) {
                // Build new inclusions: force all assignments from startPos to i-1
                List<int[]> newInclusions = new ArrayList<>(node.inclusions);
                newInclusions.addAll(node.inclusions);
                for (int j = startPos; j < i; j++) {
                    newInclusions.add(new int[]{j, currentAssignment.get(j)});
                 }

                List<int[]> newExclusions = new ArrayList<>(node.exclusions);
                newExclusions.addAll(node.exclusions);
                newExclusions.add(new int[]{i, currentAssignment.get(i)});

                int[][] modifiedMatrix = enforceConstraints(newExclusions, newInclusions);

                // Solve subproblem
                AssignmentResult result = callHungarian(modifiedMatrix);

                if (result.cost >= INF) continue;

                int actualCost = AssignmentProblem.cost(costMatrix, result.assignment);
                AssignmentResult newResult = new AssignmentResult(result.assignment, actualCost);
                pq.offer(new MurtyNode(newResult, newExclusions, newInclusions));
            }
        }
        
        return topK;
    }



    /**
     * 
     * @param exclusions: A list of (row,column) pairs that must be excluded from the solution.
     * @param inclusions: A list of (row,column) pairs that must be included in the solution.
     * @return the modified cost matrix after exclusions and inclusions are enforced (by setting costs to infinity)
     * 
     */
    public int[][] enforceConstraints(List<int[]> exclusions, List<int[]> inclusions) {
        int[][] matrix = this.problem.costMatrix;
        int n = baseMatrix.length;
        int INF = this.problem.infinity;

        if(this.baseMatrix == null || this.baseMatrix.length !=n) {
            this.baseMatrix = new int[n][n];
        }

        for(int r = 0 ; r < n ; r++) {
            System.arraycopy(matrix[r], 0, this.baseMatrix[r], 0, n);
        }
        
        for (int[] pair : inclusions) {
            int row = pair[0];
            int col = pair[1];

            this.baseMatrix[row][col] = 0;
            
            for(int j=0; j<n ;j++){
                if(j!=col) {
                    this.baseMatrix[row][j] = INF;
                }
            }

            for(int i = 0 ; i < n; i++) {
                if(i!=row) {
                    this.baseMatrix[i][col] = INF;
                }
            }
        }



        // Apply exclusions that are NOT also inclusions
    for (int[] pair : exclusions) {
        int row = pair[0], col = pair[1];

        boolean isIncluded = false;
        for (int[] inc : inclusions) {
            if (inc[0] == row && inc[1] == col) { 
                isIncluded = true; 
                break; 
            }
        }
        if (!isIncluded) { 
            this.baseMatrix[row][col] = INF;
        }
    }


        return this.baseMatrix;
    }

    /**
     * Solve the assignment problem.
     * @param matrix Cost matrix of assignment problem.
     * @return Optimal solution to the assignment problem.
     */
    AssignmentResult callHungarian(int[][] matrix) {
        long startTime = System.nanoTime();
        int[] assignment = Hungarian.solveHungarian(matrix);
        long endTime = System.nanoTime();
        this.totalCalls += 1;
        this.totalTime += endTime-startTime;

        int cost = AssignmentProblem.cost(matrix,assignment);
        AssignmentResult result = new AssignmentResult(assignment,cost);
        return result;
    }

    public void printCacheStats() {
        System.out.printf("cache hits: %d\n", this.cacheHits);
        System.out.printf("cache miss: %d\n", this.cacheMisses);
        System.out.printf("hungarian time: %.4f (%d calls)\n", this.totalTime*1e-9, this.totalCalls);
    }

}

class MurtyNode implements Comparable<MurtyNode> {
    public AssignmentResult solution;
    public List<int[]> exclusions;
    public List<int[]> inclusions;

    public MurtyNode(AssignmentResult solution, List<int[]> exclusions, List<int[]> inclusions) {
        this.solution = solution;
        this.exclusions = exclusions;
        this.inclusions = inclusions;
    }

    @Override
    public int compareTo(MurtyNode other) {
        return Integer.compare(this.result.cost, other.result.cost);
    }
}
