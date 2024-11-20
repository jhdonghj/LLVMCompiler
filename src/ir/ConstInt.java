package ir;

import static ir.type.IntegerType.INT_TYPE;

public class ConstInt extends Value {
    public int value;

    public ConstInt(int value) {
        super(INT_TYPE, Integer.toString(value));
        this.value = value;
    }
}
