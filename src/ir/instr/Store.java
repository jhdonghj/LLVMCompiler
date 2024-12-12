package ir.instr;

import ir.Value;
import mipsGen.Regs;
import mipsGen.MipsInfo;

import static ir.type.IntegerType.VOID_TYPE;
import static mipsGen.MipsInfo.loadAddress;
import static mipsGen.MipsInfo.loadValue;

public class Store extends Instr {
    public Store(String name, Value val, Value ptr) {
        super(VOID_TYPE, name, val, ptr);
    }

    @Override
    public String toString() {
        // store <ty> <value>, <ty>* <ptrval>
        Value val = operands.get(0);
        Value ptr = operands.get(1);
        return String.format("  store %s %s, %s %s", val.type, val.name, ptr.type, ptr.name);
    }

    public Value getVal() {
        return operands.get(0);
    }

    public Value getPtr() {
        return operands.get(1);
    }

    @Override
    public void to_mips() {
        super.to_mips();
        Value val = getVal(), ptr = getPtr();
        Regs val_reg = Regs.k0, ptr_reg = Regs.k1;

        val_reg = loadValue(val, val_reg);
        ptr_reg = loadAddress(ptr, ptr_reg);

        MipsInfo.store(val.type, val_reg, 0, ptr_reg);
    }
}
