package ir;

public class GlobalVariable extends Value {
    public Initializer initializer;

    public GlobalVariable(ValueType type, String name) {
        super(type, name);
    }
}
