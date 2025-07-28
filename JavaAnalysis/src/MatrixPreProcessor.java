import java.util.Arrays;

class MatrixPreProcessor{
    public static int[][] addDummies(int[][] matrix, int dummyCost) {
        int n = matrix.length;
        int size = n * 2;
        int[][] newMatrix = new int[size][size];

        for(int i = 0 ; i < size ; i ++) {
            for(int j = 0 ; j < size ; j++) {
                if(i < n && j < n) {
                    newMatrix[i][j] = matrix[i][j];
                }
                else if((i > n -1 && j > n -1)) {
                    newMatrix[i][j] = 0;
                }
                else{
                    newMatrix[i][j] = dummyCost;
                }
            }
        }

        return newMatrix;
    }

    public static int totalSum(int[][] costMatrix) {
        int total = 0;
        for(int i = 0 ; i < costMatrix.length; i ++) {
            for(int j = 0 ; j < costMatrix.length ; j++) {
                total += costMatrix[i][j];
            }
        }
        return total + 1;
    }

    public static void main(String[] args) {
        int[][] matrix = {
            {   1,    6,    9,   10 }, 
            {   5,    2,    4,   11 }, 
            {   9,    8,    3,   12 }, 
            {  15,   14,   13,    1 }  
        };
        int sumPlusOne = totalSum(matrix);
        int[][] modifiedMatrix = addDummies(matrix, sumPlusOne);
        
        for(int[] row: modifiedMatrix) {
            System.out.println(Arrays.toString(row));
        }
    }
}