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

    @Override
    public Type getElementType() {
        return elementType;
    }

    @Override
    public int getAlign() {
        return 4;
    }

    @Override
    public int getByte() {
        return 4;
    }

    @Override
    public int getSize() {
        return 4;
    }
}
