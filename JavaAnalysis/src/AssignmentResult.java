import java.util.*;
// Class to hold the assignment and its cost
public class AssignmentResult {
    public List<Integer> assignments;
    public double totalCost;

    public AssignmentResult(List<Integer> assignments, double totalCost) {
        this.assignments = assignments;
        this.totalCost = totalCost;
    }
}