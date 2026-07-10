import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;


/**
 * Algorithm for enumerating the top-k best solutions to the
 * assignment problem.  This algorithm is based on search the Order
 * Graph.
 *
 */
public class OrderGraphEnumerator {
    AssignmentProblem problem;
    OrderGraph graph;

    // stats for algorithm performance
    int totalCalls;
    long totalTime;

    /**
     * Constructor for OrderGraphEnumerator
     * @param costMatrix of the assignment problem to enumerate
     */
    public OrderGraphEnumerator(AssignmentProblem problem) {
        this.problem = problem;

        // stats for algorithm performance
        this.totalCalls = 0;
        this.totalTime = 0;
    }

    /**
     * Enumerates the top-k solutions to the assignment problem.
     * @param k The number of solutions to enumerate.
     * @return A list of solutions to the assignment problem.
     */
    public List<AssignmentSolution> enumerate(int k) {
        // initialize data structures
        List<AssignmentSolution> topK = new ArrayList<>();

        // initial call to Hungarian algorithm
        AssignmentSolution solution = callHungarian(this.problem.costMatrix);

        // initialize order graph
        this.graph = new OrderGraph(this.problem);
        OrderGraphNode root = this.graph.makeRoot(solution.cost);

        // initialize priority queue
        PriorityQueue<PQNode> pq = new PriorityQueue<>();
        Path path = Path.emptyPath();
        PQNode pqNode = new PQNode(solution.cost,path,0,root);
        pq.add(pqNode);

        while (topK.size() < k && !pq.isEmpty()) {
            // pop best solution
            pqNode = pq.poll();

            if (pqNode.path.size() == this.problem.numRows) {
                // found a leaf node
                topK.add(pqNode.solution());
                continue;
            }

            // get set of used columns
            int childIndex = 0;
            BitSet cols = pqNode.ogNode.cols;
            // generate children: try assigning the next row
            for (int col = 0; col < this.problem.numCols; col++) {
                // skip if column is already used
                if (cols.get(col)) continue;

                // update path to node
                Path newPath = pqNode.path.append(col);
                BitSet newCols = (BitSet)cols.clone(); // AC: might not scale well
                newCols.set(col);

                // check if sub-problem has been solved before
                OrderGraphNode ogNode = pqNode.ogNode;
                OrderGraphNode childOgNode;
                if (ogNode.containsChild(childIndex,newCols))
                    childOgNode = ogNode.get(childIndex);
                else {
                    int[][] newMatrix = subMatrix(newCols);
                    int solCost = callHungarian(newMatrix).cost;
                    childOgNode = ogNode.put(childIndex,newCols,solCost);
                }
                childIndex++;

                // combine cost to node and cost of node
                int pathCost = pqNode.pathCost + lastCost(newPath);
                int newCost = pathCost + childOgNode.value;
                // push child onto pq
                PQNode newNode = new PQNode(newCost,newPath,pathCost,childOgNode);
                pq.add(newNode);
            }
        }

        return topK;
    }

    /**
     * Solve the assignment problem.
     * @param matrix Cost matrix of assignment problem.
     * @return Optimal solution to the assignment problem.
     */
    AssignmentSolution callHungarian(int[][] matrix) {
        long startTime = System.nanoTime();
        AssignmentSolution solution = Hungarian.solve_alone(matrix);
        long endTime = System.nanoTime();
        this.totalCalls += 1;
        this.totalTime += endTime-startTime;

        return solution;
    }

    /**
     * This functions finds the sub-matrix of the assignment problem's
     * cost matrix found by excluding the given columns, and excluding
     * the same number of the initial rows of the matrix.  That is, it
     * returns the matrix that results from assigning the k given
     * columns to the first k rows.  (The particular assignment does
     * not matter).
     * @param cols Set of columns to _exclude_.
     * @return Corresponding sub-matrix
     */
    int[][] subMatrix(BitSet cols) {
        // cols contains set of assigned columns
        int colsSize = cols.cardinality();
        // # of assigned rows == # of assigned columns
        int rowsLeft = this.problem.numRows-colsSize;
        int colsLeft = this.problem.numCols-colsSize;
        int[][] matrix = new int[rowsLeft][colsLeft];
        int i = 0;
        for (int row = colsSize; row < this.problem.numRows; row++) {
            int j = 0;
            for (int col = 0; col < this.problem.numCols; col++) {
                if (cols.get(col)) continue;
                matrix[i][j] = this.problem.costMatrix[row][col];
                j++;
            }
            i++;
        }
        return matrix;
    }

    int lastCost(Path path) {
        int row = path.size()-1;
        int col = path.value();
        return this.problem.costMatrix[row][col];
    }

    public void printCacheStats() {
        System.out.printf("cache hits: %d\n", this.graph.cache.hits);
        System.out.printf("cache miss: %d\n", this.graph.cache.misses);
        System.out.printf("hungarian time: %.4f (%d calls)\n", this.totalTime*1e-9, this.totalCalls);
    }

    public static void main(String[] args) {
        //int n = 10;
        //int k = 3628800;
        int n = 40;
        int k = 110000;
        int bound = 10;
        int seed = 0;
        long start, end;

        System.out.printf("%d-x-%d matrix, k=%d, bound=%d\n", n, n, k, bound);

        boolean runMurtys = true;

        Random r = new Random(seed);
        int[][] costMatrix = new int[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                costMatrix[i][j] = r.nextInt(bound);

        AssignmentProblem problem = new AssignmentProblem(costMatrix);
        OrderGraphEnumerator ogEnumerator = new OrderGraphEnumerator(problem);
        MurtyEnumerator mEnumerator = new MurtyEnumerator(problem);

        List<AssignmentSolution> topK = null;
        List<AssignmentSolution> topK2 = null;

        System.out.println("enumerating...");
        start = System.nanoTime();
        topK = ogEnumerator.enumerate(k);
        end = System.nanoTime();
        System.out.printf("timer: %.4f\n", ((end-start)*1e-9));

        if (runMurtys) {
            System.out.println("enumerating (Murty's)...");
            start = System.nanoTime();
            topK2 = mEnumerator.enumerate(k);
            end = System.nanoTime();
            System.out.printf("timer: %.4f\n", ((end-start)*1e-9));
            System.out.printf("count: %d\n", topK.size());
        }

        if (runMurtys) {
            // sanity check
            boolean ok = true;
            if (topK.size() != topK2.size())
                System.out.println("check: NOT OK");
            else
                for (int i = 0; i < topK.size(); i++) {
                    AssignmentSolution r1 = topK.get(i);
                    AssignmentSolution r2 = topK2.get(i);
                    if (r1.cost != r2.cost) {
                        ok = false;
                        System.out.printf("index = %d\n", i);
                        System.out.println(r1);
                        System.out.println(r2);
                        break;
                    }
                }
            if (ok) System.out.println("check: ok");
            else    System.out.println("check: NOT OK");
        }

        /*
        // print top-k and bottom-k solutions
        //System.out.println(java.util.Arrays.deepToString(costMatrix));
        System.out.println("==1:");
        for (int i = 0; i < 10; i++) {
            System.out.println(topK.get(i));
        }
        System.out.println("==2:");
        for (int i = 0; i < 10; i++) {
            System.out.println(topK2.get(i));
        }
        System.out.println("==3:");
        for (int i = 0; i < 10; i++) {
            System.out.println(topK.get(topK.size()-i-1));
        }
        */

        System.out.println("== Order Graph Stats:");
        ogEnumerator.printCacheStats();
        if (runMurtys) {
            System.out.println("== Murty Stats:");
            mEnumerator.printCacheStats();
        }
    }
}

/**
 * Used by Order Graph Enumerator.
 */
class PQNode implements Comparable<PQNode> {
    OrderGraphNode ogNode;
    int cost;
    Path path;
    int pathCost;
    int length;
    int id;
    static int id_counter = 0;

    public PQNode(int cost, Path path, int pathCost, OrderGraphNode ogNode) {
        this.cost = cost;
        this.ogNode = ogNode;
        this.path = path;
        this.pathCost = pathCost;
        this.length = path.size();
        this.id = id_counter++;
    }

    /**
     * This is for ordering nodes in the priority queue.  Order by
     * cost.
     */
    public int compareTo(PQNode other) {
        if (this.cost < other.cost)
            return -1;
        else if (this.cost > other.cost)
            return 1;
        else  {
            if (this.length > other.length)
                return -1;
            else if (this.length < other.length)
                return 1;
            else {
                if (this.id < other.id) return -1;
                else if (this.id > other.id) return 1;
                else return 0;
            }
        }
    }

    public AssignmentSolution solution() {
        int[] sol = this.path.toArray();
        return new AssignmentSolution(sol,this.cost);
    }
}
