import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public class BenchmarkForMatrixSizeAndPerformance {
    
    public static void main(String[] args) throws IOException {
        // Fixed k values to test
        int[] kValues = {5000, 55000};
        
        // Matrix sizes from 10 to 40 with interval of 2
        int[] matrixSizes = {10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34, 36, 38, 40};
        
        // Create output file for results
        BufferedWriter writer = new BufferedWriter(new FileWriter("updated_benchmark_fixed_k_results.csv"));
        writer.write("k,n,murty_time_sec,ordergraph_time_sec,murty_calls,ordergraph_calls\n");
        
        // Test each k value
        for (int k : kValues) {
            System.out.println("\n========================================");
            System.out.println("Testing with k = " + k);
            System.out.println("========================================");
            
            // Test each matrix size
            for (int n : matrixSizes) {
                System.out.println("\nTesting n=" + n + "x" + n + ", k=" + k + "...");
                
                // Generate random matrix
                int[][] costMatrix = generateRandomMatrix(n);
                
                // Create AssignmentProblem object
                AssignmentProblem problem = new AssignmentProblem(costMatrix);
                
                // Test Murty
                double murtyTime = -1;
                int murtyCallCount = -1;
                
                try {
                    MurtyEnumerator murtyEnum = new MurtyEnumerator(problem);
                    long startMurty = System.nanoTime();
                    List<AssignmentResult> murtyResults = murtyEnum.enumerate(k);
                    long endMurty = System.nanoTime();
                    murtyTime = (endMurty - startMurty) / 1e9;
                    murtyCallCount = murtyEnum.totalCalls;
                    System.out.println("  Murty: " + String.format("%.3f", murtyTime) + " seconds (" + murtyCallCount + " calls)");
                } catch (Exception e) {
                    System.out.println("  Murty: ERROR - " + e.getMessage());
                }
                
                // Test OrderGraph
                double orderGraphTime = -1;
                int orderGraphCallCount = -1;
                
                try {
                    OrderGraphEnumerator orderGraphEnum = new OrderGraphEnumerator(problem);
                    long startOrderGraph = System.nanoTime();
                    List<AssignmentResult> orderGraphResults = orderGraphEnum.enumerate(k);
                    long endOrderGraph = System.nanoTime();
                    orderGraphTime = (endOrderGraph - startOrderGraph) / 1e9;
                    orderGraphCallCount = orderGraphEnum.totalCalls;
                    System.out.println("  OrderGraph: " + String.format("%.3f", orderGraphTime) + " seconds (" + orderGraphCallCount + " calls)");
                } catch (Exception e) {
                    System.out.println("  OrderGraph: ERROR - " + e.getMessage());
                }
                
                // Write results
                writer.write(String.format("%d,%d,%.6f,%.6f,%d,%d\n", 
                    k, n, murtyTime, orderGraphTime, murtyCallCount, orderGraphCallCount));
                writer.flush();
                
                // Optional warning if times are getting long
                if ((murtyTime > 0 && murtyTime > 180) || (orderGraphTime > 0 && orderGraphTime > 180)) {
                    System.out.println("  >>> Times exceeding 3 minutes!");
                }
            }
        }
        
        writer.close();
        System.out.println("\n=== Benchmark Complete ===");
        System.out.println("Results written to: benchmark_fixed_k_results.csv");
    }
    
    public static int[][] generateRandomMatrix(int n) {
        Random rand = new Random(42 + n); // Fixed seed for reproducibility
        int[][] matrix = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                matrix[i][j] = rand.nextInt(9000) + 1000; 
        }
        }
        return matrix;
    }
}
