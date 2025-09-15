import java.io.*;
import java.util.*;

public class Benchmark {

    public static void main(String[] args) throws IOException {
        int n = 10;
        String filename = "../../cost_matrix_9.txt"; 
        int[][] costMatrix = loadMatrixFromFile(filename, n);
        
        // Create AssignmentProblem object
        AssignmentProblem problem = new AssignmentProblem(costMatrix);

        BufferedWriter writer = new BufferedWriter(new FileWriter("updated2_benchmark_results_10x10.csv"));
        writer.write("k,murty_time_sec,ordergraph_time_sec,murty_calls,ordergraph_calls\n");

        // Initial k values for 20x20 - start smaller due to increased complexity
        int[] initialK = {10, 50, 100, 250, 500, 1000, 2500, 5000, 10000, 25000, 50000};
        
        int maxAllowedSec = 120;
        boolean murtyExceeded = false;
        double murtyMaxTime = -1;
        int murtyMaxK = -1;
        boolean orderGraphExceeded = false;
        double orderGraphMaxTime = -1;
        int orderGraphMaxK = -1;
        
        // Process initial k values
        int i = 0;
        for (; i < initialK.length; i++) {
            int k = initialK[i];
            System.out.println("\nTesting k = " + k + "...");
            
            // Test Murty only if it hasn't exceeded limit
            double murtyTime = -1;
            int murtyCallCount = -1;
            
            if (!murtyExceeded) {
                MurtyEnumerator murtyEnum = new MurtyEnumerator(problem);
                long startMurty = System.nanoTime();
                List<AssignmentResult> murtyResults = murtyEnum.enumerate(k);
                long endMurty = System.nanoTime();
                murtyTime = (endMurty - startMurty) / 1e9;
                murtyCallCount = murtyEnum.totalCalls;
                System.out.println("  Murty: " + String.format("%.2f", murtyTime) + " seconds (" + murtyCallCount + " calls)");
                
                if (murtyTime > maxAllowedSec) {
                    murtyExceeded = true;
                    murtyMaxTime = murtyTime;
                    murtyMaxK = k;
                    System.out.println("  >>> Murty exceeded " + maxAllowedSec + " second limit!");
                }
            } else {
                System.out.println("  Murty: SKIPPED (exceeded limit at k=" + murtyMaxK + ")");
            }
            
            // Test OrderGraph only if it hasn't exceeded limit
            double orderGraphTime = -1;
            int orderGraphCallCount = -1;
            
            if (!orderGraphExceeded) {
                OrderGraphEnumerator orderGraphEnum = new OrderGraphEnumerator(problem);
                long startOrderGraph = System.nanoTime();
                List<AssignmentResult> orderGraphResults = orderGraphEnum.enumerate(k);
                long endOrderGraph = System.nanoTime();
                orderGraphTime = (endOrderGraph - startOrderGraph) / 1e9;
                orderGraphCallCount = orderGraphEnum.totalCalls;
                
                System.out.println("  OrderGraph: " + String.format("%.2f", orderGraphTime) + " seconds (" + orderGraphCallCount + " calls)");
                
                if (orderGraphTime > maxAllowedSec) {
                    orderGraphExceeded = true;
                    orderGraphMaxTime = orderGraphTime;
                    orderGraphMaxK = k;
                    System.out.println("  >>> OrderGraph exceeded " + maxAllowedSec + " second limit!");
                }
            } else {
                System.out.println("  OrderGraph: SKIPPED (exceeded limit at k=" + orderGraphMaxK + ")");
            }
            
            // Write results
            writer.write(String.format("%d,%.6f,%.6f,%d,%d\n", 
                k, murtyTime, orderGraphTime, murtyCallCount, orderGraphCallCount));
            writer.flush();
            
            // Check stopping conditions - stop if both have exceeded
            if (murtyExceeded && orderGraphExceeded) {
                System.out.println("\n>>> Both algorithms exceeded " + maxAllowedSec + " seconds. Stopping benchmark.");
                break;
            }
        }
        
        // If we finished initial values and haven't hit limits, continue with dynamic k values
        if (i == initialK.length && (!murtyExceeded || !orderGraphExceeded)) {
            int k = 100000;
            int step = 50000; 
            
            while (true) {
                System.out.println("\nTesting k = " + k + "...");
                
                // Test Murty only if it hasn't exceeded limit
                double murtyTime = -1;
                int murtyCallCount = -1;
                
                if (!murtyExceeded) {
                    MurtyEnumerator murtyEnum = new MurtyEnumerator(problem);
                    long startMurty = System.nanoTime();
                    List<AssignmentResult> murtyResults = murtyEnum.enumerate(k);
                    long endMurty = System.nanoTime();
                    murtyTime = (endMurty - startMurty) / 1e9;
                    murtyCallCount = murtyEnum.totalCalls;
                    System.out.println("  Murty: " + String.format("%.2f", murtyTime) + " seconds (" + murtyCallCount + " calls)");
                    
                    if (murtyTime > maxAllowedSec) {
                        murtyExceeded = true;
                        murtyMaxTime = murtyTime;
                        murtyMaxK = k;
                        System.out.println("  >>> Murty exceeded " + maxAllowedSec + " second limit!");
                    }
                } else {
                    System.out.println("  Murty: SKIPPED (exceeded limit at k=" + murtyMaxK + ")");
                }
                
                // Test OrderGraph only if it hasn't exceeded limit
                double orderGraphTime = -1;
                int orderGraphCallCount = -1;
                
                if (!orderGraphExceeded) {
                    OrderGraphEnumerator orderGraphEnum = new OrderGraphEnumerator(problem);
                    long startOrderGraph = System.nanoTime();
                    List<AssignmentResult> orderGraphResults = orderGraphEnum.enumerate(k);
                    long endOrderGraph = System.nanoTime();
                    orderGraphTime = (endOrderGraph - startOrderGraph) / 1e9;
                    orderGraphCallCount = orderGraphEnum.totalCalls;
                    
                    System.out.println("  OrderGraph: " + String.format("%.2f", orderGraphTime) + " seconds (" + orderGraphCallCount + " calls)");
                    
                    if (orderGraphTime > maxAllowedSec) {
                        orderGraphExceeded = true;
                        orderGraphMaxTime = orderGraphTime;
                        orderGraphMaxK = k;
                        System.out.println("  >>> OrderGraph exceeded " + maxAllowedSec + " second limit!");
                    }
                } else {
                    System.out.println("  OrderGraph: SKIPPED (exceeded limit at k=" + orderGraphMaxK + ")");
                }
                
                // Write results
                writer.write(String.format("%d,%.6f,%.6f,%d,%d\n", 
                    k, murtyTime, orderGraphTime, murtyCallCount, orderGraphCallCount));
                writer.flush();
                
                // Check stopping conditions - stop if both have exceeded
                if (murtyExceeded && orderGraphExceeded) {
                    System.out.println("\n>>> Both algorithms exceeded " + maxAllowedSec + " seconds. Stopping benchmark.");
                    break;
                }
                
                // Adjust step size based on performance (use whichever is still running)
                double refTime = !orderGraphExceeded ? orderGraphTime : murtyTime;
                if (refTime > 60) {
                    step = 25000;  // Smaller steps when getting slow
                } else if (refTime > 30) {
                    step = 50000;  
                } else if (k >= 1000000) {
                    step = 200000;  // Larger steps for big k values when still fast
                }
                
                k += step;
                
                
            }
        }

        writer.close();
        System.out.println("\n=== Benchmark Complete ===");
        System.out.println("Results written to: benchmark_results_20x20.csv");
        if (murtyExceeded) {
            System.out.println("Murty exceeded limit at k=" + murtyMaxK + 
                             " with time=" + String.format("%.2f", murtyMaxTime) + " seconds");
        }
        if (orderGraphExceeded) {
            System.out.println("OrderGraph exceeded limit at k=" + orderGraphMaxK + 
                             " with time=" + String.format("%.2f", orderGraphMaxTime) + " seconds");
        }
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