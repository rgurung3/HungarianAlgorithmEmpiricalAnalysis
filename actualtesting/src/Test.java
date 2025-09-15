import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class Testing {
    public static void main(String[] args) throws Exception {
        int[][] costMatrix = loadMatrixFromFile("../../cost_matrix_20.txt", 20);
        AssignmentProblem problem = new AssignmentProblem(costMatrix);
        
        System.out.println("Testing STANDARD MurtyEnumerator (with deepCopy)");
        System.out.println("Matrix size: 20x20, k=1000000");
        
        // Force garbage collection before starting
        System.gc();
        Thread.sleep(100);
        
        // Measure memory before
        long memoryBefore = getUsedMemory();
        long startTime = System.nanoTime();
        
        // Run the enumeration
        MurtyEnumerator murty = new MurtyEnumerator(problem);
        murty.enumerate(1000);
        
        // Measure memory after
        long endTime = System.nanoTime();
        long memoryAfter = getUsedMemory();
        
        // Calculate and print results
        double timeSeconds = (endTime - startTime) / 1e9;
        long memoryUsedBytes = memoryAfter - memoryBefore;
        double memoryUsedMB = memoryUsedBytes / (1024.0 * 1024.0);
        
        System.out.println("\n=== Results ===");
        System.out.println("Time taken: " + String.format("%.2f", timeSeconds) + " seconds");
        System.out.println("Memory used: " + String.format("%.2f", memoryUsedMB) + " MB");
        System.out.println("Hungarian calls: " + murty.totalCalls);
        System.out.println("Average time per call: " + String.format("%.4f", murty.totalTime * 1e-9 / murty.totalCalls) + " seconds");
        
        // If you added allocation counting to MurtyEnumerator
        // System.out.println("Matrix allocations: " + MurtyEnumerator.matrixAllocations);
    }
    
    private static long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
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
