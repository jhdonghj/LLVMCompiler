package err;

public class ErrInfo {
    public ErrType type;
    public int line;

    public ErrInfo(ErrType type, int line) {
        this.type = type;
        this.line = line;
    }

    public String toString() {
        return String.format("%d %s", line, type);
    }
}
