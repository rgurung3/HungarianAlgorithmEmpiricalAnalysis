import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Algorithm for enumerating the top-k best solutions to the
 * assignment problem.  This algorithm is based on Murty's enumerator.
 *
 */
public class MurtyEnumerator {
    AssignmentProblem problem;
    Hungarian hungarian;
    // reusable space for cost matrix
    int[][] scratchMatrix;

    // stats for algorithm performance
    int cacheHits;
    int cacheMisses;
    int totalCalls;
    long totalTime;

    /**
     * Constructor for MurtyEnumerator
     * @param costMatrix of the assignment problem to enumerate
     */
    public MurtyEnumerator(AssignmentProblem problem) {
        this.problem = problem;
        this.hungarian  = new Hungarian(problem);
        this.scratchMatrix = new int[problem.numRows][problem.numCols];
    }

    /**
     * Enumerates the top-k solutions to the assignment problem.
     * @param k The number of solutions to enumerate.
     * @return A list of solutions to the assignment problem.
     */
    public List<AssignmentSolution> enumerate(int k) {
        // initialize data structures
        List<AssignmentSolution> topK = new ArrayList<>();
        PriorityQueue<MurtyNode> pq = new PriorityQueue<>();
        int[][] costMatrix = this.problem.costMatrix;

        // initial solution
        AssignmentSolution baseSolution = callHungarian(costMatrix);

        // initial node
        List<int[]> exclusions = new ArrayList<>();
        List<int[]> inclusions = new ArrayList<>();
        MurtyNode node = new MurtyNode(baseSolution,exclusions,inclusions);
        pq.offer(node);

        while (topK.size() < k && !pq.isEmpty()) {
            // pop best solution
            node = pq.poll();
            topK.add(node.solution);

            int[] currentAssignment = node.solution.assignment;
            int n = currentAssignment.length;

            // Find the first position that's not already forced by inclusions
            int startPos = 0;
            for (int[] inc : node.inclusions) {
                startPos = Math.max(startPos, inc[0] + 1);
            }

            for (int i = startPos; i < n; i++) {
                // Build new inclusions: force all assignments from startPos to i-1
                List<int[]> newInclusions = new ArrayList<>(node.inclusions);
                for (int j = startPos; j < i; j++)
                    newInclusions.add(new int[]{j, currentAssignment[j]});

                // Add exclusion at position i
                List<int[]> newExclusions = new ArrayList<>(node.exclusions);
                newExclusions.add(new int[]{i, currentAssignment[i]});

                // Modify cost matrix
                int[][] modifiedMatrix = enforceConstraints(newExclusions, newInclusions);

                // Solve subproblem
                AssignmentSolution solution = callHungarian(modifiedMatrix);

                // Check if the solution is infeasible based on modified cost
                if (solution.cost >= this.problem.infinity)
                    continue;

                // Calculate actual cost and add to queue
                int actualCost = AssignmentProblem.cost(costMatrix, solution.assignment);
                solution.cost = actualCost;
                pq.offer(new MurtyNode(solution, newExclusions, newInclusions));
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
        int n = this.problem.numRows;
        int m = this.problem.numCols;
        int[][] enforced = copyMatrix(this.problem.costMatrix);

        for (int[] pair : inclusions) {
            int row = pair[0];
            int col = pair[1];
            enforced[row][col] = 0;

            // Block all other cells in this row
            for (int j = 0; j < m; j++)
                if (j != col)
                    enforced[row][j] = this.problem.infinity;

            // Block all other cells in this column
            for (int i = 0; i < n; i++)
                if (i != row)
                    enforced[i][col] = this.problem.infinity;
        }

        for (int[] pair : exclusions) {
            int row = pair[0];
            int col = pair[1];
            enforced[row][col] = this.problem.infinity;
        }

        return enforced;
    }

    /**
     * Solve the assignment problem.
     * @param matrix Cost matrix of assignment problem.
     * @return Optimal solution to the assignment problem.
     */
    AssignmentSolution callHungarian(int[][] matrix) {
        long startTime = System.nanoTime();
        AssignmentSolution solution = this.hungarian.solve(matrix);
        long endTime = System.nanoTime();
        this.totalCalls += 1;
        this.totalTime += endTime-startTime;

        return solution;
    }

    int[][] copyMatrix(int[][] matrix) {
        int n = this.problem.numRows;
        int m = this.problem.numCols;
        for (int i = 0; i < n; i++)
            System.arraycopy(matrix[i],0,this.scratchMatrix[i],0,m);
        return this.scratchMatrix;
    }

    public void printCacheStats() {
        System.out.printf("cache hits: %d\n", this.cacheHits);
        System.out.printf("cache miss: %d\n", this.cacheMisses);
        System.out.printf("hungarian time: %.4f (%d calls)\n", this.totalTime*1e-9, this.totalCalls);
    }

}

class MurtyNode implements Comparable<MurtyNode> {
    public AssignmentSolution solution;
    public List<int[]> exclusions;
    public List<int[]> inclusions;

    public MurtyNode(AssignmentSolution solution, List<int[]> exclusions, List<int[]> inclusions) {
        this.solution = solution;
        this.exclusions = exclusions;
        this.inclusions = inclusions;
    }

    @Override
    public int compareTo(MurtyNode other) {
        return Integer.compare(this.solution.cost, other.solution.cost);
    }
}
