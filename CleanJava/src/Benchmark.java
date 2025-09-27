import java.util.List;
import java.util.Random;

class Benchmark {
    public static int range = 100;

    public static int[][] randomMatrix(Random r, int n) {
        int[][] matrix = new int[n][n];

        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                matrix[i][j] = r.nextInt(range);

        return matrix;
    }

    public static void test(long seed, int n, int k) {
        Random r = new Random(seed);

        int[][] matrix = randomMatrix(r,n);

        AssignmentProblem problem = new AssignmentProblem(matrix);
        MurtyEnumerator enumerator = new MurtyEnumerator(problem);

        long start = System.nanoTime();
        List<AssignmentSolution> solutions = enumerator.enumerate(k);
        long end = System.nanoTime();
        double time = (end-start)*1e-6;


        System.out.println("=====");
        System.out.printf("seed: %d\n", seed);
        System.out.printf("n,k: %d,%d\n", n, k);
        System.out.printf("time: %.4fms\n", time);
        System.out.printf("top-k solutions:\n");
        if (k<=100) {
            int i = 1;
            for (AssignmentSolution sol : solutions) {
                System.out.printf("(%d,%d,%s)\n", i++, sol.cost, sol.assignment.toString());
            }

        } else if (k<=1000) {
            int i = 1;
            for (AssignmentSolution sol : solutions)
                System.out.printf("(%d,%d) ", i++, sol.cost);
            System.out.println();
        } else {

        }
    }

    public static void main(String[] args) {
        test(0,10,100);
        test(0,10,1000);
        test(0,10,3628800);
    }
}
