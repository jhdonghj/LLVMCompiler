package ir.instr;

import ir.Value;
import ir.type.Type;
import mipsGen.Regs;
import mipsGen.mipsInfo;

import static mipsGen.mipsInfo.loadValue;
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

        reg = loadValue(src, reg);

        if (!mipsInfo.value2offset.containsKey(this)) {
            mipsInfo.alloc(destType);
            mipsInfo.value2offset.put(this, mipsInfo.cur_offset);
            if (destType.getByte() == 4) {
                writeln(String.format("    sw $%s, %d($sp)", reg, mipsInfo.cur_offset));
            } else {
                writeln(String.format("    sb $%s, %d($sp)", reg, mipsInfo.cur_offset));
            }
        }
    }
}
