package ir.instr;

import ir.Value;
import ir.type.PointerType;
import mipsGen.Regs;
import mipsGen.MipsInfo;

import static ir.type.IntegerType.BOOL_TYPE;
import static ir.type.IntegerType.INT_TYPE;
import static mipsGen.MipsInfo.storeValue;
import static utils.IO.writeln;
import static mipsGen.MipsInfo.loadValue;

public class Icmp extends Instr {
    public enum Op {
        EQ, NE,
        SLE, SLT, SGE, SGT, // signed
        ULE, ULT, UGE, UGT // unsigned (no use)
    }

    public Op op;

    public Icmp(String name, Op op, Value lhs, Value rhs) {
        super(BOOL_TYPE, name, lhs, rhs);
        this.op = op;
    }

    @Override
    public String toString() {
        // %name = icmp op i32 %lhs, %rhs
        Value lhs = operands.get(0);
        Value rhs = operands.get(1);
        return String.format("  %s = icmp %s i32 %s, %s", name, op.toString().toLowerCase(), lhs.name, rhs.name);
    }

    @Override
    public void to_mips() {
        super.to_mips();
        Value lhs = operands.get(0), rhs = operands.get(1);
        Regs reg1 = Regs.k0, reg2 = Regs.k1, target = Regs.k0;

        reg1 = loadValue(lhs, reg1);
        reg2 = loadValue(rhs, reg2);

        switch (op) {
            case EQ:
                writeln(String.format("    seq $%s, $%s, $%s", target, reg1, reg2));
                break;
            case NE:
                writeln(String.format("    sne $%s, $%s, $%s", target, reg1, reg2));
                break;
            case SLE:
                writeln(String.format("    sle $%s, $%s, $%s", target, reg1, reg2));
                break;
            case SLT:
                writeln(String.format("    slt $%s, $%s, $%s", target, reg1, reg2));
                break;
            case SGE:
                writeln(String.format("    sge $%s, $%s, $%s", target, reg1, reg2));
                break;
            case SGT:
                writeln(String.format("    sgt $%s, $%s, $%s", target, reg1, reg2));
                break;
        }

//        storeValue(this, target);
        if (!MipsInfo.value2reg.containsKey(this.name)) {
            MipsInfo.alloc(new PointerType(INT_TYPE));
            MipsInfo.value2offset.put(this.name, MipsInfo.cur_offset);
            writeln(String.format("    sw $%s, %d($sp)", target, MipsInfo.value2offset.get(this.name)));
        }
    }
}
