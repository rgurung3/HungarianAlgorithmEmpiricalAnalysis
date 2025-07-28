import java.io.*;
import java.util.*;

public class EnumeratorBenchMark {

    public static void main(String[] args) throws IOException {
        int n = 10;
        int[] ks = {1000, 5000, 10000, 25000, 50000, 100000, 500000, 1000000, 2000000, 3000000, 3700000};
        int runsPerK = 1;

        String filename = "../../cost_matrix_0.txt";
        int[][] costMatrix = loadMatrixFromFile(filename, n);

        BufferedWriter writer = new BufferedWriter(new FileWriter("../java_results.csv"));
        writer.write("matrix_id,k,avg_time_sec,avg_hungarian_calls\n");

        for (int k : ks) {
            double totalTime = 0.0;
            long totalHungarianCalls = 0;

            for (int run = 0; run < runsPerK; run++) {
                hungarianAlgo.callCount = 0;

                long start = System.nanoTime();
                List<AssignmentResult> results = Enumeration.getTopKMurtys(costMatrix, k);
                long end = System.nanoTime();

                double elapsedSec = (end - start) / 1e9;
                totalTime += elapsedSec;
                totalHungarianCalls += hungarianAlgo.callCount;
            }

            double avgTime = totalTime / runsPerK;
            double avgCalls = (double) totalHungarianCalls / runsPerK;

            String line = String.format("%d,%d,%.6f,%.1f", 0, k, avgTime, avgCalls);
            System.out.println(line);
            writer.write(line + "\n");
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
