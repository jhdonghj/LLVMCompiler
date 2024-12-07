package ir.instr;

import ir.GlobalVariable;
import ir.Value;
import ir.type.PointerType;
import mipsGen.Regs;
import mipsGen.mipsInfo;

import static mipsGen.mipsInfo.loadAddress;
import static utils.IO.writeln;

public class Load extends Instr {
    public Load(String name, Value ptr) {
        super(((PointerType) ptr.type).elementType, name, ptr);
    }

    @Override
    public String toString() {
        // <result> = load <ty>, <ty>* <ptrval>
        Value ptr = operands.get(0);
        return String.format("  %s = load %s, %s %s", name, type, ptr.type, ptr.name);
    }

    @Override
    public void to_mips() {
        super.to_mips();
        Value ptr = operands.get(0);
        Regs pointer_reg = Regs.k0, target_reg = mipsInfo.value2reg.getOrDefault(this, Regs.k0);

        loadAddress(ptr, pointer_reg);

        mipsInfo.load(type, target_reg, 0, pointer_reg);
        if (!mipsInfo.value2reg.containsKey(this)) {
            mipsInfo.alloc(type);
            mipsInfo.value2offset.put(this, mipsInfo.cur_offset);
            mipsInfo.store(type, target_reg, mipsInfo.value2offset.get(this), Regs.sp);
        }
    }
}
