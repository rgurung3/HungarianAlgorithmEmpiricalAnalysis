import java.io.*;
import java.util.*;

public class EnumeratorBenchmark {

    public static void main(String[] args) throws IOException {
        int n = 10;
        int[] ks = {1000, 5000, 10000, 25000, 50000, 100000, 500000, 1000000, 2000000, 3000000, 3700000};
        int runsPerK = 10;  

        String filename = "../../cost_matrix_0.txt"; 
        int[][] costMatrix = loadMatrixFromFile(filename, n);

        BufferedWriter writer = new BufferedWriter(new FileWriter("../java_results.csv"));
        writer.write("matrix_id,k,avg_time_sec\n");

        for (int k : ks) {
            double totalTime = 0.0;

            for (int run = 0; run < runsPerK; run++) {
                long start = System.nanoTime();
                List<AssignmentResult> results = Enumeration.getTopKMurtys(costMatrix, k);
                long end = System.nanoTime();

                double elapsedSec = (end - start) / 1000000000.0;
                totalTime += elapsedSec;
            }

            double avgTime = totalTime / runsPerK;
            String line = String.format("%d,%d,%.6f", 0, k, avgTime); 
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
