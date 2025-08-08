import java.io.*;
import java.util.*;

public class EnumeratorBenchmark {

    public static void main(String[] args) throws IOException {
        int n = 20;
        int[] ks = {1000, 5000, 10000, 25000, 50000, 100000, 500000, 1000000, 2000000, 3000000, 3700000};

        String filename = "../../cost_matrix_20.txt"; 
        int[][] costMatrix = loadMatrixFromFile(filename, n);

        BufferedWriter writer = new BufferedWriter(new FileWriter("../benchmark_results.csv"));
        writer.write("k,murty_time_sec,ordertree_time_sec,murty_calls,ordertree_calls\n");

        for (int k : ks) {
            hungarianAlgo.callCount =0;
            long startMurty = System.nanoTime();
            List<AssignmentResult> murtyResults = Enumeration.getTopKMurtys(costMatrix, k);
            long endMurty = System.nanoTime();
            double murtyTime = (endMurty - startMurty) / 1e9;
            int murtyCallCount = hungarianAlgo.callCount;

            OrderTreeEnumerator enumerator = new OrderTreeEnumerator(costMatrix);
            long startOrderTree = System.nanoTime();
            List<AssignmentResult> orderTreeResults = enumerator.enumerate(k);
            long endOrderTree = System.nanoTime();
            double orderTreeTime = (endOrderTree - startOrderTree) / 1e9;
            int orderTreeCallCount = enumerator.totalCalls;
            writer.write(String.format("%d,%.6f,%.6f,%d,%d\n", 
                k, murtyTime, orderTreeTime, murtyCallCount, orderTreeCallCount));
            writer.flush();
        }

        writer.close();
    }

    public static int[][] loadMatrixFromFile(String filename, int n) throws IOException {
        int[][] matrix = new int[n][n];
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        int i = 0;
        while ((line = br.readLine()) != null && i < n) {
            String[] tokens = line.trim().split("\\s+");
            for (int j = 0; j < n; j++) {
                matrix[i][j] = Integer.parseInt(tokens[j]);
            }
            i++;
        }
        br.close();
        return matrix;
    }
}
