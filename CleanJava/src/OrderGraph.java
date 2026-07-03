import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;

public class OrderGraph {
    OrderGraphCache cache;
    int size; // number of elements in the order

    public OrderGraph(AssignmentProblem problem) {
        this.size = problem.numRows;
        this.cache = new OrderGraphCache(this);
    }

    public OrderGraphNode makeRoot(int value) {
        BitSet bs = new BitSet(this.size);
        OrderGraphNode root = new OrderGraphNode(this,bs,value);
        this.cache.put(bs,root);
        return root;
    }
}

class OrderGraphNode {
    OrderGraph og;
    BitSet cols;
    int graphSize;
    int nodeSize;
    int value;
    OrderGraphNode[] children;

    public OrderGraphNode(OrderGraph og, BitSet cols, int value) {
        this.og = og;
        this.cols = cols; // key
        this.graphSize = og.size;
        this.nodeSize = cols.cardinality();
        this.value = value;
        //this.children = new OrderGraphNode[graphSize-nodeSize];
        this.children = null;
    }

    public int value() { return this.value; }

    public boolean containsChild(int index, BitSet cols) {
        if (this.children == null) {
            int num_children = this.graphSize-this.nodeSize;
            this.children = new OrderGraphNode[num_children];
        }
        if (this.children[index] != null)
            return true;
        OrderGraphNode node = this.og.cache.get(cols);
        if (node == null)
            return false;
        else {
            this.children[index] = node;
            return true;
        }
    }

    public OrderGraphNode get(int index) {
        return this.children[index];
    }

    public OrderGraphNode put(int index, BitSet cols, int value) {
        OrderGraphNode child = new OrderGraphNode(this.og,cols,value);
        this.children[index] = child;
        this.og.cache.put(cols,child);
        return child;
    }
}

class OrderGraphCache {
    ArrayList<HashMap<BitSet,OrderGraphNode>> cache;
    int size;
    int hits;
    int misses;

    public OrderGraphCache(OrderGraph graph) {
        // initialize cache
        this.size = graph.size;
        this.cache = new ArrayList<>(this.size+1);
        for (int i = 0; i <= this.size; i++)
            cache.add(new HashMap<BitSet,OrderGraphNode>());
        // prime cache with solved problem, which has 0 remaining cost
        BitSet trivialSet = allCols(size);
        int value = 0;
        OrderGraphNode sink = new OrderGraphNode(graph,trivialSet,value);
        cache.get(size).put(trivialSet,sink);

        this.hits = 0;
        this.misses = 0;
    }

    /**
     * @param numCols number of columns
     * @return A set containing all integers from 0..numCols-1
     */
    static BitSet allCols(int numCols) {
        BitSet all = new BitSet(numCols);
        all.set(0,numCols);
        return all;
    }

    public OrderGraphNode contains(BitSet key) {
        int len = key.cardinality();
        OrderGraphNode node = this.cache.get(len).get(key);
        if (node == null) this.misses++;
        else              this.hits++;
        return node;
    }

    public OrderGraphNode get(BitSet key) {
        return contains(key);
    }

    public void put(BitSet key, OrderGraphNode value) {
        int len = key.cardinality();
        this.cache.get(len).put(key,value);
    }
}
