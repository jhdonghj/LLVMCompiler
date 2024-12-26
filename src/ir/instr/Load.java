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
        Regs pointer_reg = loadAddress(ptr, Regs.k0);
        Regs target_reg = MipsInfo.value2reg.getOrDefault(this.name, Regs.k0);

        MipsInfo.load(type, target_reg, 0, pointer_reg);

        storeValue(this, target_reg);
    }
}
