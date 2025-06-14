package ir.instr;

import ir.Value;
import ir.type.Type;
import mipsGen.Regs;
import mipsGen.MipsInfo;

import static mipsGen.MipsInfo.*;
import static utils.IO.writeln;

public class Zext extends Instr {
    public Type destType;

    public Zext(String name, Value src, Type destType) {
        super(destType, name, src);
        this.destType = destType;
    }

    @Override
    public String toString() {
        // <result> = zext <ty> <value> to <ty2>
        Value src = operands.get(0);
        return String.format("  %s = zext %s %s to %s", name, src.type, src.name, destType);
    }

    @Override
    public void to_mips() {
        super.to_mips();
        Value src = operands.get(0);
        Regs reg = Regs.k0;

        reg = loadValue(src, reg);

        storeValue(this, reg);
    }
}
