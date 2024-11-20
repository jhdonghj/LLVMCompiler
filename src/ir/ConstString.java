package ir;

import ir.type.ArrayType;
import ir.type.PointerType;

import static ir.type.IntegerType.CHAR_TYPE;
import static irGen.IrGen.new_constString;
import static utils.IO.writeln;

public class ConstString extends Value {
    public String value;

    public ConstString(String name, String value) {
        super(new PointerType(new ArrayType(value.length() + 1, CHAR_TYPE)), name);
        this.value = value;
        new_constString(this);
    }

    public void print() {
        writeln(name + " = constant " + ((PointerType) type).elementType + " c\"" +
                value.replaceAll("\n", "\\\\0A") + "\\00\"");
    }
}
