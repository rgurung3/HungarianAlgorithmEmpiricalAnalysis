import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MatrixEntry {
    String name;
    int[][] matrix;

    public MatrixEntry(String name, int[][] matrix) {
        this.name = name;
        this.matrix = matrix;
    }

    public static List<MatrixEntry> loadMatrices(String filename) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filename));
        List<MatrixEntry> matrices = new ArrayList<>();

        List<int[]> currentMatrix = new ArrayList<>();
        String currentName = null;

        for (String line : lines) {
            line = line.replace("\uFEFF", "").trim();
            if (line.isEmpty()) {
                // End of one matrix
                if (!currentMatrix.isEmpty()) {
                    matrices.add(new MatrixEntry(currentName, currentMatrix.toArray(new int[0][])));
                    currentMatrix.clear();
                }
            } else if (line.startsWith("#")) {
                // Header line
                currentName = line.substring(1).trim();
            } else {

                // Regular matrix row
                int[] row = Arrays.stream(line.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .mapToInt(Integer::parseInt)
                        .toArray();
                currentMatrix.add(row);

            }
        }

        return matrices;
    }
}
