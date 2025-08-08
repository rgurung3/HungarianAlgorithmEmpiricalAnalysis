import java.io.*;
import java.util.*;

public class EnumeratorBenchMark20x20 {

    public static void main(String[] args) throws IOException {
        int n = 20;
        String filename = "../../cost_matrix_20.txt"; 
        int[][] costMatrix = loadMatrixFromFile(filename, n);

        BufferedWriter writer = new BufferedWriter(new FileWriter("../benchmark_results_20x20.csv"));
        writer.write("k,murty_time_sec,ordertree_time_sec,murty_calls,ordertree_calls\n");

        // Initial k values for 20x20
        int[] initialK = {100, 500, 1000, 2500, 5000, 10000, 25000, 50000, 100000};
        
        int maxAllowedSec = 120;
        boolean orderTreeExceeded = false;
        double orderTreeMaxTime = -1;
        int orderTreeMaxK = -1;
        
        // Process initial k values
        int i = 0;
        for (; i < initialK.length; i++) {
            int k = initialK[i];
            System.out.println("\nTesting k = " + k + "...");
            
            // Test Murty
            hungarianAlgo.callCount = 0;
            long startMurty = System.nanoTime();
            List<AssignmentResult> murtyResults = Enumeration.getTopKMurtys(costMatrix, k);
            long endMurty = System.nanoTime();
            double murtyTime = (endMurty - startMurty) / 1e9;
            int murtyCallCount = hungarianAlgo.callCount;
            System.out.println("  Murty: " + String.format("%.2f", murtyTime) + " seconds (" + murtyCallCount + " calls)");
            
            // Test OrderTree only if it hasn't exceeded limit
            double orderTreeTime = -1;
            int orderTreeCallCount = -1;
            
            if (!orderTreeExceeded) {
                OrderTreeEnumerator enumerator = new OrderTreeEnumerator(costMatrix);
                long startOrderTree = System.nanoTime();
                List<AssignmentResult> orderTreeResults = enumerator.enumerate(k);
                long endOrderTree = System.nanoTime();
                orderTreeTime = (endOrderTree - startOrderTree) / 1e9;
                orderTreeCallCount = enumerator.totalCalls;
                
                System.out.println("  OrderTree: " + String.format("%.2f", orderTreeTime) + " seconds (" + orderTreeCallCount + " calls)");
                
                if (orderTreeTime > maxAllowedSec) {
                    orderTreeExceeded = true;
                    orderTreeMaxTime = orderTreeTime;
                    orderTreeMaxK = k;
                    System.out.println("  >>> OrderTree exceeded " + maxAllowedSec + " second limit!");
                }
            } else {
                System.out.println("  OrderTree: SKIPPED (exceeded limit at k=" + orderTreeMaxK + ")");
            }
            
            // Write results
            writer.write(String.format("%d,%.6f,%.6f,%d,%d\n", 
                k, murtyTime, orderTreeTime, murtyCallCount, orderTreeCallCount));
            writer.flush();
            
            // Check stopping conditions
            if (murtyTime > maxAllowedSec) {
                System.out.println("\n>>> Murty also exceeded " + maxAllowedSec + " seconds. Stopping benchmark.");
                break;
            }
            
            if (orderTreeExceeded && orderTreeMaxTime > 0 && murtyTime >= orderTreeMaxTime * 0.9) {
                System.out.println("\n>>> Murty time is approaching OrderTree's max time. Stopping benchmark.");
                break;
            }
        }
        
        // If we finished initial values and haven't hit limits, continue with dynamic k values
        if (i == initialK.length) {
            int k = 200000; 
            int step = 100000;
            
            while (true) {
                System.out.println("\nTesting k = " + k + "...");
                
                // Test Murty
                hungarianAlgo.callCount = 0;
                long startMurty = System.nanoTime();
                List<AssignmentResult> murtyResults = Enumeration.getTopKMurtys(costMatrix, k);
                long endMurty = System.nanoTime();
                double murtyTime = (endMurty - startMurty) / 1e9;
                int murtyCallCount = hungarianAlgo.callCount;
                System.out.println("  Murty: " + String.format("%.2f", murtyTime) + " seconds (" + murtyCallCount + " calls)");
                
                // Test OrderTree only if it hasn't exceeded limit
                double orderTreeTime = -1;
                int orderTreeCallCount = -1;
                
                if (!orderTreeExceeded) {
                    OrderTreeEnumerator enumerator = new OrderTreeEnumerator(costMatrix);
                    long startOrderTree = System.nanoTime();
                    List<AssignmentResult> orderTreeResults = enumerator.enumerate(k);
                    long endOrderTree = System.nanoTime();
                    orderTreeTime = (endOrderTree - startOrderTree) / 1e9;
                    orderTreeCallCount = enumerator.totalCalls;
                    
                    System.out.println("  OrderTree: " + String.format("%.2f", orderTreeTime) + " seconds (" + orderTreeCallCount + " calls)");
                    
                    if (orderTreeTime > maxAllowedSec) {
                        orderTreeExceeded = true;
                        orderTreeMaxTime = orderTreeTime;
                        orderTreeMaxK = k;
                        System.out.println("  >>> OrderTree exceeded " + maxAllowedSec + " second limit!");
                    }
                } else {
                    System.out.println("  OrderTree: SKIPPED (exceeded limit at k=" + orderTreeMaxK + ")");
                }
                
                // Write results
                writer.write(String.format("%d,%.6f,%.6f,%d,%d\n", 
                    k, murtyTime, orderTreeTime, murtyCallCount, orderTreeCallCount));
                writer.flush();
                
                // Check stopping conditions
                if (murtyTime > maxAllowedSec) {
                    System.out.println("\n>>> Murty exceeded " + maxAllowedSec + " seconds. Stopping benchmark.");
                    break;
                }
                
                if (orderTreeExceeded && orderTreeMaxTime > 0 && murtyTime >= orderTreeMaxTime * 0.9) {
                    System.out.println("\n>>> Murty time (" + String.format("%.2f", murtyTime) + 
                                     "s) is approaching OrderTree's max time (" + 
                                     String.format("%.2f", orderTreeMaxTime) + "s). Stopping benchmark.");
                    break;
                }
                
                // Adjust step size based on performance
                if (murtyTime > 60) {
                    step = 50000;  // Smaller steps when getting slow
                } else if (murtyTime > 30) {
                    step = 75000;  
                } else if (k >= 1000000) {
                    step = 200000;  // Larger steps for big k values when still fast
                }
                
                k += step;
                
                // Safety check - don't go beyond reasonable limits
                if (k > 10000000) {
                    System.out.println("\n>>> Reached maximum k limit (10M). Stopping benchmark.");
                    break;
                }
            }
        }

        writer.close();
        if (orderTreeExceeded) {
            System.out.println("OrderTree exceeded limit at k=" + orderTreeMaxK + 
                             " with time=" + String.format("%.2f", orderTreeMaxTime) + " seconds");
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