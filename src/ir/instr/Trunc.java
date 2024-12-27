package ir.instr;

import ir.Value;
import ir.type.Type;
import mipsGen.Regs;
import mipsGen.MipsInfo;

import static mipsGen.MipsInfo.*;
import static utils.IO.writeln;

public class Trunc extends Instr {
    public Type destType;

    public Trunc(String name, Value src, Type destType) {
        super(destType, name, src);
        this.destType = destType;
    }

    @Override
    public String toString() {
        // <result> = trunc <ty> <value> to <ty2>
        Value src = operands.get(0);
        return String.format("  %s = trunc %s %s to %s", name, src.type, src.name, destType);
    }

    @Override
    public void to_mips() {
        super.to_mips();
        Value src = operands.get(0);
        Regs reg = Regs.k0;
        Regs target = MipsInfo.value2reg.getOrDefault(this.name, Regs.k0);

        reg = loadValue(src, reg);

        if (src.type.getByte() == 4 && destType.getByte() == 1) {
            writeln(String.format("    andi $%s, $%s, 0xff", target, reg));
        }

        storeValue(this, target);
    }
}
