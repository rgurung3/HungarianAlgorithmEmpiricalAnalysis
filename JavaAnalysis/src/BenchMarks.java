import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BenchMarks {
    public static void run(String matrixName, int[][] costMatrix, int runs) {
        List<Long> times = new ArrayList<>();
        hungarianAlgo.solveHungarian(costMatrix);

        for (int i = 0; i < runs; i++) {
            long start = System.nanoTime();
            hungarianAlgo.solveHungarian(costMatrix);
            long end = System.nanoTime();
            times.add(end - start);
        }

        double avg = times.stream().mapToLong(Long::longValue).average().orElse(0);
        long min = Collections.min(times);
        long max = Collections.max(times);
        double stdDev = Math.sqrt(times.stream()
                .mapToDouble(t -> Math.pow(t - avg, 2))
                .average().orElse(0));

        System.out.printf("Matrix: %s%n", matrixName);
        System.out.printf("Avg Time: %.3f ms | Min: %.3f ms | Max: %.3f ms | StdDev: %.3f ms%n",
                avg / 1_000_000.0, min / 1_000_000.0, max / 1_000_000.0, stdDev / 1_000_000.0);
        System.out.println();
    }
}
