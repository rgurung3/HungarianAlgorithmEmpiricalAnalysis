import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class Testing {
    public static void main(String[] args) throws Exception {
        int[][] costMatrix = loadMatrixFromFile("../../cost_matrix_20.txt", 20);
        AssignmentProblem problem = new AssignmentProblem(costMatrix);
        
	System.out.println("Testing STANDARD MurtyEnumerator (without deepCopy)");
        System.out.println("Matrix size: 20x20, k=1000000");
        
       
        // Run the enumeration
        MurtyEnumerator murty = new MurtyEnumerator(problem);
        murty.enumerate(50000);
        
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
