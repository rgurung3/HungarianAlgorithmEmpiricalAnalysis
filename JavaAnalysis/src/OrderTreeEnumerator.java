import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.PriorityQueue;

public class OrderTreeEnumerator {
    int[][] costMatrix;
    int numRows;
    int numCols; 

    int cacheHits;
    int cacheMisses;
    public int totalCalls;
    long totalTime;

    public OrderTreeEnumerator(int[][] costMatrix) {
        this.costMatrix = costMatrix;
        this.numRows = costMatrix.length;
        this.numCols = costMatrix[0].length;

        this.cacheHits = 0;
        this.cacheMisses = 0;
        this.totalCalls = 0;
        this.totalTime = 0;
    }

    public List<AssignmentResult> enumerate(int k) {
        List<AssignmentResult> topK = new ArrayList<>();
        PriorityQueue<OrderTreeNode> pq = new PriorityQueue<>();
        HashMap<HashSet<Integer>,Integer> cache = new HashMap<>();
        cache.put(allCols(this.numCols),0);

        long startTime = System.nanoTime();
        int[] assignment = hungarianAlgo.solveHungarian(this.costMatrix);
        long endTime = System.nanoTime();
        this.totalCalls += 1;
        this.totalTime += endTime-startTime;

        int cost = cost(costMatrix,assignment);
        List<Integer> path = new ArrayList<Integer>();
        OrderTreeNode node = new OrderTreeNode(cost,path);
        pq.add(node);

        while (topK.size() < k && !pq.isEmpty()) {
            node = pq.poll();
            if (node.path.size() == numRows) {
                topK.add(node.result());
                continue;
            }
            HashSet<Integer> cols = new HashSet<Integer>(node.path);

            for (int col = 0; col < this.numCols; col++) {
                if (cols.contains(col)) continue;
                List<Integer> newPath = new ArrayList<Integer>(node.path);
                newPath.add(col);
                HashSet<Integer> newCols = new HashSet<Integer>(newPath);

                int pathCost = cost(costMatrix,newPath);
                int solCost = 0;
                if (cache.containsKey(newCols)) {
                    this.cacheHits += 1;
                    solCost = cache.get(newCols);
                } else {
                    this.cacheMisses += 1;
                    int[][] newMatrix = subMatrix(newCols);

                    startTime = System.nanoTime();
                    int[] sol = hungarianAlgo.solveHungarian(newMatrix);
                    endTime = System.nanoTime();
                    this.totalCalls += 1;
                    this.totalTime += endTime-startTime;

                    solCost = cost(newMatrix,sol);
                    cache.put(newCols,solCost);
                }
                int newCost = pathCost + solCost;
                OrderTreeNode newNode = new OrderTreeNode(newCost,newPath);
                pq.add(newNode);
            }
        }

        return topK;
    }

    static int cost(int[][] matrix, int[] assignment) {
        int cost = 0;
        for (int i = 0; i < assignment.length; i++)
            cost += matrix[i][assignment[i]];
        return cost;
    }

    static int cost(int[][] matrix, List<Integer> assignment) {
        int cost = 0;
        for (int i = 0; i < assignment.size(); i++)
            cost += matrix[i][assignment.get(i)];
        return cost;
    }

    static HashSet<Integer> allCols(int numCols) {
        HashSet<Integer> all = new HashSet<Integer>(numCols);
        for (int col = 0; col < numCols; col++)
            all.add(col);
        return all;
    }

    int[][] subMatrix(HashSet<Integer> cols) {
        // cols contains set of assigned columns
        int colsSize = cols.size();
        // # of assigned rows == # of assigned columns
        int rowsLeft = this.numRows-colsSize;
        int colsLeft = this.numCols-colsSize;
        int[][] matrix = new int[rowsLeft][colsLeft];
        int i = 0;
        for (int row = colsSize; row < this.numRows; row++) {
            int j = 0;
            for (int col = 0; col < this.numCols; col++) {
                if (cols.contains(col)) continue;
                matrix[i][j] = this.costMatrix[row][col];
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

    public static void main(String[] args) {
        /*
        int[][] costMatrix = new int[][] {
            {1, 5, 9},
            {6, 2, 8},
            {7, 4, 3}
        };
        int k = 6;
        */

        /*
        int[][] costMatrix = new int[][] {
            {5,0,3,3,7,9,3,5},
            {2,4,7,6,8,8,1,6},
            {7,7,8,1,5,9,8,9},
            {4,3,0,3,5,0,2,3},
            {8,1,3,3,3,7,0,1},
            {9,9,0,4,7,3,2,7},
            {2,0,0,4,5,5,6,8},
            {4,1,4,9,8,1,1,7}};
        int k = 40320;
        */

        /*
        int[][] costMatrix = new int[][] {
            {5,0,3,3,7,9,3,5,2},
            {4,7,6,8,8,1,6,7,7},
            {8,1,5,9,8,9,4,3,0},
            {3,5,0,2,3,8,1,3,3},
            {3,7,0,1,9,9,0,4,7},
            {3,2,7,2,0,0,4,5,5},
            {6,8,4,1,4,9,8,1,1},
            {7,9,9,3,6,7,2,0,3},
            {5,9,4,4,6,4,4,3,4}};
        int k = 362880;
        */

        int[][] costMatrix = new int[][] {
            {5,0,3,3,7,9,3,5,2,4},
            {7,6,8,8,1,6,7,7,8,1},
            {5,9,8,9,4,3,0,3,5,0},
            {2,3,8,1,3,3,3,7,0,1},
            {9,9,0,4,7,3,2,7,2,0},
            {0,4,5,5,6,8,4,1,4,9},
            {8,1,1,7,9,9,3,6,7,2},
            {0,3,5,9,4,4,6,4,4,3},
            {4,4,8,4,3,7,5,5,0,1},
            {5,9,3,0,5,0,1,2,4,2}};
        int k = 3628800;


        OrderTreeEnumerator enumerator = new OrderTreeEnumerator(costMatrix);


        System.out.println("enumerating...");
        long start = System.nanoTime();
        List<AssignmentResult> topK = enumerator.enumerate(k);
        long end = System.nanoTime();
        System.out.printf("timer: %.4f\n", ((end-start)*1e-9));

        /*
        System.out.println(java.util.Arrays.deepToString(costMatrix));
        for (int i = 0; i < 10; i++) {
            System.out.println(topK.get(i));
        }
        for (int i = 0; i < 10; i++) {
            System.out.println(topK.get(topK.size()-i-1));
        }
        */
        enumerator.printCacheStats();
    }
}


class OrderTreeNode implements Comparable<OrderTreeNode> {
    int cost;
    List<Integer> path;
    int length;

    public OrderTreeNode(int cost, List<Integer> path) {
        this.cost = cost;
        this.path = path;
        this.length = path.size();
    }

    public int compareTo(OrderTreeNode other) {
        if (this.cost < other.cost)
            return -1;
        else if (this.cost == other.cost)
            if (this.length < other.length)
                return -1;
            else if (this.length == other.length) {
                for (int i = 0; i < this.length; i++)
                    if (this.path.get(i) < other.path.get(i))
                        return -1;
                    else if (this.path.get(i) > other.path.get(i))
                        return 1;
                return 0;
            }
        return 1;
    }

    static List<Integer> asList(int[] array) {
        List<Integer> list = new ArrayList<>();
        for (int num : array)
            list.add(num);
        return list;
    }

    public AssignmentResult result() {
        List<Integer> path = new ArrayList<Integer>(this.path);
        int cost = this.cost;
        return new AssignmentResult(path,cost);

    }
}

