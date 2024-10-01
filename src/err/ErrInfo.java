package err;

public class ErrInfo implements Comparable<ErrInfo> {
    public ErrType type;
    public int line;

    public ErrInfo(ErrType type, int line) {
        this.type = type;
        this.line = line;
    }

    public String toString() {
        return String.format("%d %s", line, type);
    }

    public int compareTo(ErrInfo other) {
        return this.line - other.line;
    }
}
