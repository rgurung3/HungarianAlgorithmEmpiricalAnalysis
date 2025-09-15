import java.util.ArrayList;
import java.util.List;

public class AssignmentResult {
    public List<Integer> assignment;
    public int cost;

    public AssignmentResult(List<Integer> assignment, int cost) {
        this.assignment = assignment;
        this.cost = cost;
    }

    public AssignmentResult(int[] assignment, int cost) {
        this.assignment = asList(assignment);
        this.cost = cost;
    }

    public boolean equals(AssignmentResult other) {
        return this.cost == other.cost && 
            this.assignment.equals(other.assignment);
    }

    public String toString() {
        return String.format("%.4f: %s", this.cost, this.assignment);
    }

    static List<Integer> asList(int[] array) {
        List<Integer> list = new ArrayList<>(array.length);
        for (int i = 0; i < array.length; i++)
            list.add(array[i]);
        return list;
    }

}
