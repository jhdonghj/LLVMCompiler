package ir.instr;

import ir.Value;
import ir.type.PointerType;
import mipsGen.Regs;
import mipsGen.mipsInfo;

import static ir.type.IntegerType.INT_TYPE;
import static utils.IO.writeln;
import static mipsGen.mipsInfo.loadValue;

public class BinaryOperator extends Instr {
    public enum Op {
        ADD, SUB, MUL, SDIV, SREM, AND, OR, XOR, SLL, SRL, SRA, MULSH
    }

    public Op op;

    public BinaryOperator(String name, Op op, Value lhs, Value rhs) {
        super(INT_TYPE, name, lhs, rhs);
        this.op = op;
    }

    @Override
    public String toString() {
        // %name = op_lower i32 %lhs, %rhs
        Value lhs = operands.get(0);
        Value rhs = operands.get(1);
        return String.format("  %s = %s i32 %s, %s", name, op.toString().toLowerCase(), lhs.name, rhs.name);
    }

    @Override
    public void to_mips() {
        super.to_mips();
        Value lhs = operands.get(0), rhs = operands.get(1);
        Regs reg1 = Regs.k0, reg2 = Regs.k1, target = Regs.k0;

        reg1 = loadValue(lhs, reg1);
        reg2 = loadValue(rhs, reg2);

        switch (op) {
            case ADD:
                writeln(String.format("    addu $%s, $%s, $%s", target, reg1, reg2));
                break;
            case SUB:
                writeln(String.format("    subu $%s, $%s, $%s", target, reg1, reg2));
                break;
            case MUL:
                writeln(String.format("    mul $%s, $%s, $%s", target, reg1, reg2));
                break;
            case SDIV:
                writeln(String.format("    div $%s, $%s", reg1, reg2));
                writeln(String.format("    mflo $%s", target));
                break;
            case SREM:
                writeln(String.format("    div $%s, $%s", reg1, reg2));
                writeln(String.format("    mfhi $%s", target));
                break;
            case AND:
                writeln(String.format("    and $%s, $%s, $%s", target, reg1, reg2));
                break;
            case OR:
                writeln(String.format("    or $%s, $%s, $%s", target, reg1, reg2));
                break;
            case XOR:
                writeln(String.format("    xor $%s, $%s, $%s", target, reg1, reg2));
                break;
            case SLL:
                writeln(String.format("    sllv $%s, $%s, $%s", target, reg1, reg2));
                break;
            case SRL:
                writeln(String.format("    srlv $%s, $%s, $%s", target, reg1, reg2));
                break;
            case SRA:
                writeln(String.format("    srav $%s, $%s, $%s", target, reg1, reg2));
                break;
            case MULSH:
                writeln(String.format("    mult $%s, $%s", reg1, reg2));
                writeln(String.format("    mfhi $%s", target));
                break;
        }
        if (!mipsInfo.value2reg.containsKey(this)) {
            mipsInfo.alloc(new PointerType(INT_TYPE));
            mipsInfo.value2offset.put(this, mipsInfo.cur_offset);
            writeln(String.format("    sw $%s, %d($sp)", target, mipsInfo.value2offset.get(this)));
        }
    }
}
