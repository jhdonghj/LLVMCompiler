package ir.instr;

import ir.Value;
import ir.type.Type;

import static utils.IO.writeln;

public class Zext extends Instr {
    public Type destType;

    public Zext(String name, Value src, Type destType) {
        super(destType, name, src);
        this.destType = destType;
    }

    @Override
    public void print() {
        // <result> = zext <ty> <value> to <ty2>
        Value src = operands.get(0);
        writeln(String.format("  %s = zext %s %s to %s", name, src.type, src.name, destType));
    }
}
