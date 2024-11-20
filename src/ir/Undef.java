package ir;

import static ir.type.IntegerType.INT_TYPE;

public class Undef extends Value {
    public Undef() {
        super(INT_TYPE, "undef");
    }
}
