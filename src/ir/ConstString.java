package ir;

public class ConstString extends Value {
    public String value;

    public ConstString(ValueType type, String name, String value) {
        super(type, name);
        this.value = value;
    }
}
