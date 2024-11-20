package ir.instr;

import ir.Value;

import static ir.type.IntegerType.VOID_TYPE;
import static utils.IO.writeln;

public class Store extends Instr {
    public Store(String name, Value val, Value ptr) {
        super(VOID_TYPE, name, val, ptr);
    }

    @Override
    public void print() {
        // store <ty> <value>, <ty>* <ptrval>
        Value val = operands.get(0);
        Value ptr = operands.get(1);
        writeln(String.format("  store %s %s, %s %s", val.type, val.name, ptr.type, ptr.name));
    }
}
