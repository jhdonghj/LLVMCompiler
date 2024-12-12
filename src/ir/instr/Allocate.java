package ir.instr;

import ir.Initializer;
import ir.type.PointerType;
import ir.type.Type;
import mipsGen.MipsInfo;
import mipsGen.Regs;

import static utils.IO.writeln;

public class Allocate extends Instr {
    public Type allocType;
    public Initializer initializer;

    public Allocate(String name, Type allocType) {
        super(new PointerType(allocType), name);
        this.allocType = allocType;
    }

    public Allocate(String name, Type allocType, Initializer initializer) {
        super(new PointerType(allocType), name);
        this.allocType = allocType;
        this.initializer = initializer;
    }

    @Override
    public String toString() {
        // %name = alloca type
        return String.format("  %s = alloca %s", name, allocType);
    }

    @Override
    public void to_mips() {
        super.to_mips();
        MipsInfo.alloc(allocType);
        if (MipsInfo.value2reg.containsKey(this.name)) {
            Regs pointerReg = MipsInfo.value2reg.get(this.name);
            writeln(String.format("    addi $%s, $sp, %d", pointerReg, MipsInfo.cur_offset));
        } else {
            writeln(String.format("    addi $k0, $sp, %d", MipsInfo.cur_offset));
            MipsInfo.alloc(new PointerType(allocType));
            MipsInfo.value2offset.put(this.name, MipsInfo.cur_offset);
            writeln(String.format("    sw $k0, %d($sp)", MipsInfo.value2offset.get(this.name)));
        }
    }
}
