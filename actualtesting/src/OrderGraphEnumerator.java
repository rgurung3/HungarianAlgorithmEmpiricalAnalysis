import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;


public class OrderGraphEnumerator {
    AssignmentProblem problem;

    // stats for algorithm performance
    int cacheHits;
    int cacheMisses;
    public int totalCalls;
    public long totalTime;

    /**
     * Constructor for OrderGraphEnumerator
     * @param costMatrix of the assignment problem to enumerate
     */
    public OrderGraphEnumerator(AssignmentProblem problem) {
        this.problem = problem;

        // stats for algorithm performance
        this.cacheHits = 0;
        this.cacheMisses = 0;
        this.totalCalls = 0;
        this.totalTime = 0;
    }

    /**
     * Enumerates the top-k solutions to the assignment problem.
     * @param k The number of solutions to enumerate.
     * @return A list of solutions to the assignment problem.
     */
    public List<AssignmentResult> enumerate(int k) {
        // initialize data structures
        List<AssignmentResult> topK = new ArrayList<>();
        ArrayList<HashMap<BitSet,Integer>> cache = 
            new ArrayList<>(this.problem.numRows+1);
        for (int i = 0; i <= this.problem.numRows; i++)
            cache.add(new HashMap<BitSet,Integer>());
        // prime cache with solved problem, which has 0 remaining cost
        BitSet trivialSet = allCols(this.problem.numCols);
        cache.get(this.problem.numRows).put(trivialSet,0);

        // initial call to Hungarian algorithm
        AssignmentResult result = callHungarian(this.problem.costMatrix);

        // initialize priority queue
        PriorityQueue<OrderGraphNode> pq = new PriorityQueue<>();
        List<Integer> path = new ArrayList<Integer>();
        OrderGraphNode node = new OrderGraphNode(result.cost,path);
        pq.add(node);

        while (topK.size() < k && !pq.isEmpty()) {
            // pop best solution
            node = pq.poll();

            if (node.path.size() == this.problem.numRows) {
                // found a leaf node
                topK.add(node.result());
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
                int depth = newPath.size();
                int solCost = 0;
                if (cache.get(depth).containsKey(newCols)) {
                    this.cacheHits += 1;
                    solCost = cache.get(depth).get(newCols);
                } else {
                    this.cacheMisses += 1;
                    int[][] newMatrix = subMatrix(newCols);
                    result = callHungarian(newMatrix);
                    cache.get(depth).put(newCols,result.cost);
                    solCost = result.cost;
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

    /**
     * @param numCols number of columns
     * @return A set containing all integers from 0..numCols-1
     */
    static BitSet allCols(int numCols) {
        BitSet all = new BitSet(numCols);
        for (int col = 0; col < numCols; col++)
            all.set(col);
        return all;
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
        System.out.printf("cache hits: %d\n", this.cacheHits);
        System.out.printf("cache miss: %d\n", this.cacheMisses);
        System.out.printf("hungarian time: %.4f (%d calls)\n", this.totalTime*1e-9, this.totalCalls);
    }

//     public static void main(String[] args) {
//         /*
//         int[][] costMatrix = new int[][] {
//             {1, 5, 9},
//             {6, 2, 8},
//             {7, 4, 3}
//         };
//         int k = 6;
//         */

//         /*
//         int[][] costMatrix = new int[][] {
//             {5,0,3,3,7,9,3,5},
//             {2,4,7,6,8,8,1,6},
//             {7,7,8,1,5,9,8,9},
//             {4,3,0,3,5,0,2,3},
//             {8,1,3,3,3,7,0,1},
//             {9,9,0,4,7,3,2,7},
//             {2,0,0,4,5,5,6,8},
//             {4,1,4,9,8,1,1,7}};
//         int k = 40320;
//         */

//         /*
//         int[][] costMatrix = new int[][] {
//             {5,0,3,3,7,9,3,5,2},
//             {4,7,6,8,8,1,6,7,7},
//             {8,1,5,9,8,9,4,3,0},
//             {3,5,0,2,3,8,1,3,3},
//             {3,7,0,1,9,9,0,4,7},
//             {3,2,7,2,0,0,4,5,5},
//             {6,8,4,1,4,9,8,1,1},
//             {7,9,9,3,6,7,2,0,3},
//             {5,9,4,4,6,4,4,3,4}};
//         int k = 362880;
//         */

//         /*
//         int[][] costMatrix = new int[][] {
//             {5,0,3,3,7,9,3,5,2,4},
//             {7,6,8,8,1,6,7,7,8,1},
//             {5,9,8,9,4,3,0,3,5,0},
//             {2,3,8,1,3,3,3,7,0,1},
//             {9,9,0,4,7,3,2,7,2,0},
//             {0,4,5,5,6,8,4,1,4,9},
//             {8,1,1,7,9,9,3,6,7,2},
//             {0,3,5,9,4,4,6,4,4,3},
//             {4,4,8,4,3,7,5,5,0,1},
//             {5,9,3,0,5,0,1,2,4,2}};
//         int k = 3628800;
//         */

//         /*
//         int[][] costMatrix = new int[][] {
//             {5,0,3,3,7,9,3,5,2,4,7,6,8,8,1,6,7,7,8,1},
//             {5,9,8,9,4,3,0,3,5,0,2,3,8,1,3,3,3,7,0,1},
//             {9,9,0,4,7,3,2,7,2,0,0,4,5,5,6,8,4,1,4,9},
//             {8,1,1,7,9,9,3,6,7,2,0,3,5,9,4,4,6,4,4,3},
//             {4,4,8,4,3,7,5,5,0,1,5,9,3,0,5,0,1,2,4,2},
//             {0,3,2,0,7,5,9,0,2,7,2,9,2,3,3,2,3,4,1,2},
//             {9,1,4,6,8,2,3,0,0,6,0,6,3,3,8,8,8,2,3,2},
//             {0,8,8,3,8,2,8,4,3,0,4,3,6,9,8,0,8,5,9,0},
//             {9,6,5,3,1,8,0,4,9,6,5,7,8,8,9,2,8,6,6,9},
//             {1,6,8,8,3,2,3,6,3,6,5,7,0,8,4,6,5,8,2,3},
//             {9,7,5,3,4,5,3,3,7,9,9,9,7,3,2,3,9,7,7,5},
//             {1,2,2,8,1,5,8,4,0,2,5,5,0,8,1,1,0,3,8,8},
//             {4,4,0,9,3,7,3,2,1,1,2,1,4,2,5,5,5,2,5,7},
//             {7,6,1,6,7,2,3,1,9,5,9,9,2,0,9,1,9,0,6,0},
//             {4,8,4,3,3,8,8,7,0,3,8,7,7,1,8,4,7,0,4,9},
//             {0,6,4,2,4,6,3,3,7,8,5,0,8,5,4,7,4,1,3,3},
//             {9,2,5,2,3,5,7,2,7,1,6,5,0,0,3,1,9,9,6,6},
//             {7,8,8,7,0,8,6,8,9,8,3,6,1,7,4,9,2,0,8,2},
//             {7,8,4,4,1,7,6,9,4,1,5,9,7,1,3,5,7,3,6,6},
//             {7,9,1,9,6,0,3,8,4,1,4,5,0,3,1,4,4,4,0,0}};
//         int k = 100000;
//         */

//         AssignmentProblem problem = new AssignmentProblem(costMatrix);
//         OrderGraphEnumerator ogEnumerator = new OrderGraphEnumerator(problem);
//         MurtyEnumerator mEnumerator = new MurtyEnumerator(problem);


//         //List<AssignmentResult> topK = new ArrayList<>();
//         //long start, end;

//         System.out.println("enumerating...");
//         long start = System.nanoTime();
//         List<AssignmentResult> topK = ogEnumerator.enumerate(k);
//         long end = System.nanoTime();
//         System.out.printf("timer: %.4f\n", ((end-start)*1e-9));


//         System.out.println("enumerating (Murty's)...");
//         start = System.nanoTime();
//         List<AssignmentResult> topK2 = mEnumerator.enumerate(k);
//         end = System.nanoTime();
//         System.out.printf("timer: %.4f\n", ((end-start)*1e-9));
//         System.out.printf("count: %d\n", topK.size());

//         // sanity check
//         boolean ok = true;
//         if (topK.size() != topK2.size())
//             System.out.println("check: NOT OK");
//         else
//             for (int i = 0; i < topK.size(); i++) {
//                 AssignmentResult r1 = topK.get(i);
//                 AssignmentResult r2 = topK2.get(i);
//                 if (r1.cost != r2.cost) {
//                 //if (!r1.equals(r2)) {
//                     ok = false;
//                     break;
//                 }
//             }

//         if (ok)
//             System.out.println("check: ok");
//         else
//             System.out.println("check: NOT OK");

//         /*
//         // print top-k and bottom-k solutions
//         System.out.println(java.util.Arrays.deepToString(costMatrix));
//         for (int i = 0; i < 10; i++) {
//             System.out.println(topK.get(i));
//         }
//         for (int i = 0; i < 10; i++) {
//             System.out.println(topK.get(topK.size()-i-1));
//         }
//         */

//         System.out.println("== Order Graph Stats:");
//         ogEnumerator.printCacheStats();
//         System.out.println("== Murty Stats:");
//         mEnumerator.printCacheStats();
//     }
}

/**
 * Used by Order Graph Enumerator.
 */
class OrderGraphNode implements Comparable<OrderGraphNode> {
    int cost;
    List<Integer> path;
    int length;

    public OrderGraphNode(int cost, List<Integer> path) {
        this.cost = cost;
        this.path = path;
        this.length = path.size();
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
                for (int i = 0; i < this.length; i++)
                    if (this.path.get(i) < other.path.get(i))
                        return -1;
                    else if (this.path.get(i) > other.path.get(i))
                        return 1;
                return 0;
            }
        }
    }

    public AssignmentResult result() {
        List<Integer> path = new ArrayList<Integer>(this.path);
        int cost = this.cost;
        return new AssignmentResult(path,cost);

    }
}
