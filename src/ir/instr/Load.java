package ir.instr;

import ir.Value;
import ir.type.PointerType;

import static utils.IO.writeln;

public class Load extends Instr {
    public Load(String name, Value ptr) {
        super(((PointerType) ptr.type).elementType, name, ptr);
    }

    @Override
    public void print() {
        // <result> = load <ty>, <ty>* <ptrval>
        Value ptr = operands.get(0);
        writeln(String.format("  %s = load %s, %s %s", name, type, ptr.type, ptr.name));
    }
}
