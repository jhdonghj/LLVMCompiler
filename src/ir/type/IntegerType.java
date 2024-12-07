package ir.type;

public class IntegerType extends Type {
    public static final Type VOID_TYPE = new IntegerType(0);
    public static final Type INT_TYPE = new IntegerType(32);
    public static final Type CHAR_TYPE = new IntegerType(8);
    public static final Type BOOL_TYPE = new IntegerType(1);

    public int bitWidth;

    public IntegerType(int bitWidth) {
        this.bitWidth = bitWidth;
    }

    @Override
    public String toString() {
        return bitWidth > 0 ? "i" + bitWidth : "void";
    }

    @Override
    public int getAlign() {
        return getByte();
    }

    @Override
    public int getByte() {
        return bitWidth / 8;
    }

    @Override
    public int getSize() {
        return getByte();
    }
}
