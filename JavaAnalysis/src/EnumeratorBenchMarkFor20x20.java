import java.io.*;
import java.util.*;

public class EnumeratorBenchMarkFor20x20 {

    public static void main(String[] args) throws IOException {
        int n = 20;
        String filename = "../../cost_matrix_20.txt";
        int[][] costMatrix = loadMatrixFromFile(filename, n);

        BufferedWriter writer = new BufferedWriter(new FileWriter("../final_java_results_20x20.csv"));
        writer.write("matrix_id,k,time_sec\n");

        int matrixId = 0;
        int k = 100000;
        int step = 100000;
        int maxAllowedSec = 120;

        while (true) {
            long start = System.nanoTime();
            List<AssignmentResult> results = Enumeration.getTopKMurtys(costMatrix, k);
            long end = System.nanoTime();

            double elapsedSec = (end - start) / 1e9;
            String line = String.format("%d,%d,%.6f", matrixId, k, elapsedSec);
            System.out.println(line);
            writer.write(line + "\n");
            writer.flush();

            if (elapsedSec > maxAllowedSec) {
                System.out.println("Stopping early: k = " + k + " exceeded " + maxAllowedSec + " seconds.");
                break;
            }

            k += step;
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
