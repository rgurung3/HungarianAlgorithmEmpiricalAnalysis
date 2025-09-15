import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Testing {
    
    public static void main(String[] args) throws Exception {
        // Load your matrix
        int[][] costMatrix = 
        
        // Reset the counter (important!)
        matrixAllocations = 0;
        
        System.out.println("Testing with deepCopy implementation");
        
        // Run the test
        List<AssignmentResult> results = getTopKMurtys(costMatrix, 1000000);
        
        // Print the allocation count
        System.out.println("Matrix allocations (deepCopy calls): " + matrixAllocations);
        System.out.println("Results found: " + results.size());
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