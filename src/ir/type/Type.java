package ir.type;

public class Type {

    public boolean isPointer() {
        return this instanceof PointerType;
    }

    public boolean isArray() {
        return this instanceof ArrayType;
    }

    public boolean isInt() {
        return this instanceof IntegerType && ((IntegerType) this).bitWidth == 32;
    }

    public boolean isChar() {
        return this instanceof IntegerType && ((IntegerType) this).bitWidth == 8;
    }

    public boolean isBool() {
        return this instanceof IntegerType && ((IntegerType) this).bitWidth == 1;
    }

    public boolean isVoid() {
        return this instanceof IntegerType && ((IntegerType) this).bitWidth == 0;
    }

    public boolean isFunction() {
        return this instanceof FunctionType;
    }

    public boolean isBB() {
        return this instanceof BBType;
    }

    public Type getElementType() { return null; }

    public int getAlign() { return -1; }

    public int getByte() { return -1; }

    public int getSize() { return -1; }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Type) {
            return this.toString().equals(obj.toString());
        }
        return false;
    }
}
