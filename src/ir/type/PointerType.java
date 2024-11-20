package ir.type;

public class PointerType extends Type {
    public Type elementType;

    public PointerType(Type elementType) {
        this.elementType = elementType;
    }

    @Override
    public String toString() {
        return elementType + "*";
    }
}
