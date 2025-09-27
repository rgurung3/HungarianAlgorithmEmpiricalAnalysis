import java.util.ArrayList;
import java.util.List;

/**
 * This class defines a solution to the Assignment Problem.
 *
 */
public class AssignmentSolution {
    //public List<Integer> assignment;
    public int[] assignment;
    public int cost;

    boolean modelFailures;
    public List<int[]> matches;
    public List<int[]> falsePositives;
    public List<int[]> falseNegatives;

    public AssignmentSolution(int[] assignment, int cost) {
        //this.assignment = assignment.clone();
        this.assignment = assignment;
        this.cost = cost;
        this.modelFailures = false;
    }

    public AssignmentSolution(int[] assignment, int cost,
                              List<int[]> matches,
                              List<int[]> falsePositives,
                              List<int[]> falseNegatives) {
        this.assignment = assignment.clone();
        this.cost = cost;
        this.modelFailures = true;
        this.matches = matches;
        this.falsePositives = falsePositives;
        this.falseNegatives = falseNegatives;
    }

    public boolean equals(AssignmentSolution other) {
        return this.cost == other.cost && 
            this.assignment.equals(other.assignment);
    }

    public String toString() {
        return String.format("%.4f: %s", this.cost, java.util.Arrays.toString(this.assignment));
    }

    static List<Integer> asList(int[] array) {
        List<Integer> list = new ArrayList<>(array.length);
        for (int i = 0; i < array.length; i++)
            list.add(array[i]);
        return list;
    }

    public AssignmentSolution extractFailures(AssignmentProblem problem) {
        if (problem.modelFailures == false)
            return this;

        List<int[]> matches = new ArrayList<>();
        List<int[]> falsePositives = new ArrayList<>();
        List<int[]> falseNegatives = new ArrayList<>();
        
        for (int i = 0; i < this.assignment.length; i++) {
            int j = this.assignment[i];

            if (i < problem.numRows && j < problem.numCols) // true positive
                matches.add(new int[]{i, j});
            else if (i < problem.numRows && j >= problem.numCols)
                falseNegatives.add(new int[]{i, j - problem.numCols});
            else if (i >= problem.numRows && j < problem.numCols)
                falsePositives.add(new int[]{j, i - problem.numRows});
        }

        return new AssignmentSolution(this.assignment,this.cost,
                                      matches,falsePositives,falseNegatives);
    }


}
