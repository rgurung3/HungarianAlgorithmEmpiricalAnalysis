import java.util.*;

public class MurtyNode implements Comparable<MurtyNode> {
    public AssignmentResult result;
    public List<int[]> exclusions;
    public List<int[]> inclusions;

    public MurtyNode(AssignmentResult result, List<int[]> exclusions, List<int[]> inclusions) {
        this.result = result;
        this.exclusions = exclusions;
        this.inclusions = inclusions;
    }

    @Override
    public int compareTo(MurtyNode other) {
        return Double.compare(this.result.totalCost, other.result.totalCost);
    }
}
