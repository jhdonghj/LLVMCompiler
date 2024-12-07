package ir.type;

public class ArrayType extends Type {
    public int size; // size = -1 means incomplete array type(eg. int a[] in function parameter)
    public Type elementType;

    public ArrayType(int size, Type elementType) {
        this.size = size;
        this.elementType = elementType;
    }

    @Override
    public String toString() {
        return "[" + size + " x " + elementType + "]";
    }

    @Override
    public Type getElementType() {
        return elementType;
    }

    @Override
    public int getAlign() {
        return elementType.getAlign();
    }

    @Override
    public int getByte() {
        return elementType.getByte();
    }

    @Override
    public int getSize() {
        return size * elementType.getSize();
    }
}
