import java.util.BitSet;

public class Path {
    Path next;
    int size;
    int value;

    Path() {
        this.next = null;
        this.size = 0;
        this.value = -1;
    }

    Path(Path next, int value) {
        this.next = next;
        this.size = next.size + 1;
        this.value = value;
    }

    public static Path emptyPath() {
        return new Path();
    }

    public boolean isEnd() { return this.size == 0; }
    public Path next() { return this.next; }
    public int size() { return this.size; }
    public int value() { return this.value; }
    public Path append(int value) { return new Path(this,value); }

    public int[] toArray() {
        Path p = this;
        int[] ar = new int[p.size()];
        int i = p.size()-1;

        while ( !p.isEnd() ) {
            ar[i--] = p.value();
            p = p.next();
        }
        return ar;
    }

    public BitSet toBitSet() {
        Path p = this;
        BitSet bs = new BitSet(p.size());
        while ( !p.isEnd() ) {
            bs.set(p.value());
            p = p.next();
        }
        return bs;
    }
}
