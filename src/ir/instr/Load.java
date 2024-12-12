package ir.instr;

import ir.Value;
import ir.type.PointerType;
import mipsGen.Regs;
import mipsGen.MipsInfo;

import static mipsGen.MipsInfo.loadAddress;
import static mipsGen.MipsInfo.storeValue;

public class Load extends Instr {
    public Load(String name, Value ptr) {
        super(((PointerType) ptr.type).elementType, name, ptr);
    }

    public Value getPtr() {
        return operands.get(0);
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
        Regs pointer_reg = Regs.k0, target_reg = MipsInfo.value2reg.getOrDefault(this, Regs.k0);

        pointer_reg = loadAddress(ptr, pointer_reg);

        MipsInfo.load(type, target_reg, 0, pointer_reg);

//        storeValue(this, target_reg);
        if (!MipsInfo.value2reg.containsKey(this.name)) {
            MipsInfo.alloc(type);
            MipsInfo.value2offset.put(this.name, MipsInfo.cur_offset);
            MipsInfo.store(type, target_reg, MipsInfo.value2offset.get(this.name), Regs.sp);
        }
    }
}
