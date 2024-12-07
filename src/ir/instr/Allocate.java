package ir.instr;

import ir.Initializer;
import ir.type.PointerType;
import ir.type.Type;
import mipsGen.mipsInfo;

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
        mipsInfo.alloc(allocType);
        if (mipsInfo.value2reg.containsKey(this)) {
            // to do
        } else {
            writeln(String.format("    addi $k0, $sp, %d", mipsInfo.cur_offset));
            mipsInfo.alloc(new PointerType(allocType));
            mipsInfo.value2offset.put(this, mipsInfo.cur_offset);
            writeln(String.format("    sw $k0, %d($sp)", mipsInfo.value2offset.get(this)));
        }
    }
}
