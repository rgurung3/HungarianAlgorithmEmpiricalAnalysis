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
    OrderGraphCache cache;

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
        this.cache = new OrderGraphCache(this.problem.numRows,
                                         this.problem.numCols);

        // initial call to Hungarian algorithm
        AssignmentSolution solution = callHungarian(this.problem.costMatrix);

        // initialize priority queue
        PriorityQueue<OrderGraphNode> pq = new PriorityQueue<>();
        List<Integer> path = new ArrayList<Integer>();
        OrderGraphNode node = new OrderGraphNode(solution.cost,path);
        pq.add(node);

        while (topK.size() < k && !pq.isEmpty()) {
            // pop best solution
            node = pq.poll();

            if (node.path.size() == this.problem.numRows) {
                // found a leaf node
                topK.add(node.solution());
                continue;
            }

            // get set of used columns
            BitSet cols = pathToBitSet(node.path);
            // generate children: try assigning the next row
            for (int col = 0; col < this.problem.numCols; col++) {
                // skip if column is already used
                if (cols.get(col)) continue;

                // update path to node
                List<Integer> newPath = new ArrayList<>(node.path);
                newPath.add(col);
                BitSet newCols = (BitSet)cols.clone();
                newCols.set(col);

                // check if sub-problem has been solved before
                int solCost = 0;
                if (this.cache.contains(newCols))
                    solCost = this.cache.get(newCols);
                else {
                    int[][] newMatrix = subMatrix(newCols);
                    solCost = callHungarian(newMatrix).cost;
                    this.cache.put(newCols,solCost);
                }

                // combine cost to node and cost of node
                int pathCost = this.problem.cost(newPath);
                int newCost = pathCost + solCost;
                // push child onto pq
                OrderGraphNode newNode = new OrderGraphNode(newCost,newPath);
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

    static BitSet pathToBitSet(List<Integer> path) {
        BitSet bs = new BitSet(path.size());
        for (int i : path)
            bs.set(i);
        return bs;
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

    public void printCacheStats() {
        System.out.printf("cache hits: %d\n", this.cache.hits);
        System.out.printf("cache miss: %d\n", this.cache.misses);
        System.out.printf("hungarian time: %.4f (%d calls)\n", this.totalTime*1e-9, this.totalCalls);
    }

    public static void main(String[] args) {
        //int n = 10;
        //int k = 3628800;
        int n = 40;
        int k = 10000;
        int bound = 10;
        int seed = 0;


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

        //List<AssignmentSolution> topK = new ArrayList<>();
        //long start, end;

        System.out.println("enumerating...");
        long start = System.nanoTime();
        topK = ogEnumerator.enumerate(k);
        long end = System.nanoTime();
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
                        //if (!r1.equals(r2)) {
                        ok = false;
                        break;
                    }
                }
            if (ok) System.out.println("check: ok");
            else    System.out.println("check: NOT OK");
        }

        /*
        // print top-k and bottom-k solutions
        System.out.println(java.util.Arrays.deepToString(costMatrix));
        for (int i = 0; i < 10; i++) {
            System.out.println(topK.get(i));
        }
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
class OrderGraphNode implements Comparable<OrderGraphNode> {
    int cost;
    List<Integer> path; //AC: to array?
    int length;
    int id;
    static int id_counter = 0;

    public OrderGraphNode(int cost, List<Integer> path) {
        this.cost = cost;
        this.path = path;
        this.length = path.size();
        this.id = id_counter++;
    }

    /**
     * This is for ordering nodes in the priority queue.  Order by
     * cost.
     */
    public int compareTo(OrderGraphNode other) {
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
        int[] path = new int[this.path.size()];
        for (int i = 0; i < this.path.size(); i++)
            path[i] = this.path.get(i);
        int cost = this.cost;
        return new AssignmentSolution(path,cost);

    }
}

class OrderGraphCache {
    ArrayList<HashMap<BitSet,Integer>> cache;
    int hits;
    int misses;

    public OrderGraphCache(int numRows, int numCols) {
        // initialize cache
        this.cache = new ArrayList<>(numRows+1);
        for (int i = 0; i <= numRows; i++)
            cache.add(new HashMap<BitSet,Integer>());
        // prime cache with solved problem, which has 0 remaining cost
        BitSet trivialSet = allCols(numCols);
        cache.get(numRows).put(trivialSet,0);

        this.hits = 0;
        this.misses = 0;
    }

    /**
     * @param numCols number of columns
     * @return A set containing all integers from 0..numCols-1
     */
    static BitSet allCols(int numCols) {
        BitSet all = new BitSet(numCols);
        all.set(0,numCols);
        return all;
    }

    public boolean contains(BitSet key) {
        int len = key.cardinality();
        if (this.cache.get(len).containsKey(key)) {
            this.hits++;
            return true;
        } else {
            this.misses++;
            return false;
        }
    }

    public int get(BitSet key) {
        int len = key.cardinality();
        return this.cache.get(len).get(key);
    }

    public void put(BitSet key, int value) {
        int len = key.cardinality();
        this.cache.get(len).put(key,value);
    }
}
