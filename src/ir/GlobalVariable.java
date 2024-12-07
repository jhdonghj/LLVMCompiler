package ir;

import ir.type.ArrayType;
import ir.type.PointerType;
import ir.type.Type;

import static irGen.IrGen.new_globalVariable;
import static utils.IO.writeln;

public class GlobalVariable extends Value {
    public Initializer initializer;

    public GlobalVariable(Type type, String name, Initializer initializer) {
        super(type, name);
        this.initializer = initializer;
        new_globalVariable(this);
    }

    public void print() {
        writeln(name + " = dso_local global " + initializer);
    }

    public void to_mips() {
        Type type = ((PointerType) this.type).elementType;
        if (type.isArray()) type = ((ArrayType) type).elementType;
        if (type.isInt()) {
            writeln(String.format("    %s: .word %s", name.substring(1), initializer.to_mips()));
        } else {
            writeln(String.format("    %s: .byte %s", name.substring(1), initializer.to_mips()));
        }
    }
}
