package ir;

public class ConstInt extends Value {
    public int value;

    public ConstInt(int value) {
        super(null, null);
        this.value = value;
    }
}
