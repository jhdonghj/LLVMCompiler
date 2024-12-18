package ir.instr;

import ir.ConstInt;
import ir.Value;
import mipsGen.Regs;
import mipsGen.MipsInfo;

import java.util.HashSet;

import static ir.type.IntegerType.INT_TYPE;
import static mipsGen.MipsInfo.*;
import static utils.IO.writeln;

public class BinaryOperator extends Instr {
    static class MagicNumber {
        public long M; // Magic number
        public int sh_post; // 位移数
        public int l;

        public MagicNumber(long M, int sh_post, int l) {
            this.M = M; this.sh_post = sh_post; this.l = l;
        }
    }
    private static final int N = 32;
    private static MagicNumber choose_multiplier(int d, int prec) {
        int l = 1;
        while ((1 << l) < d) l++;
        int sh_post = l;
        long m_low = (1L << (N + l)) / d, m_high = ((1L << (N + l)) + (1L << (N + l - prec))) / d;
        while (m_low / 2 < m_high / 2 && sh_post > 0) {
            m_low /= 2; m_high /= 2; sh_post--;
        }
        return new MagicNumber(m_high, sh_post, l);
    }

    private static void do_div(Regs target, Regs src, int d) {
        MagicNumber res = choose_multiplier(Math.abs(d), N - 1);
        if (Math.abs(d) == 1) {
            move(target, src);
        } else if (Math.abs(d) == (1L << res.l)) {
            writeln(String.format("    sra $%s, $%s, %d", Regs.k1, src, res.l - 1));
            writeln(String.format("    srl $%s, $%s, %d", Regs.k1, Regs.k1, N - res.l));
            writeln(String.format("    addu $%s, $%s, $%s", target, src, Regs.k1));
            writeln(String.format("    sra $%s, $%s, %d", target, target, res.l));
        } else {
            if (res.M < (1L << (N - 1))) {
                writeln(String.format("    li $%s, %d", Regs.k1, res.M));
                writeln(String.format("    mult $%s, $%s", src, Regs.k1));
                writeln(String.format("    mfhi $%s", Regs.k1));
                writeln(String.format("    sra $%s, $%s, %d", Regs.k1, Regs.k1, res.sh_post));
            } else {
                writeln(String.format("    li $%s, %d", Regs.k1, res.M - (1L << N)));
                writeln(String.format("    mult $%s, $%s", src, Regs.k1));
                writeln(String.format("    mfhi $%s", Regs.k1));
                writeln(String.format("    addu $%s, $%s, $%s", Regs.k1, Regs.k1, src));
                writeln(String.format("    sra $%s, $%s, %d", Regs.k1, Regs.k1, res.sh_post));
            }
            writeln(String.format("    srl $%s, $%s, %d", Regs.v1, src, N - 1));
            writeln(String.format("    addu $%s, $%s, $%s", target, Regs.k1, Regs.v1));
        }
        if (d < 0) {
            writeln(String.format("    subu $%s, $0, $%s", target, target));
        }
    }

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
        Regs reg1 = Regs.k0, reg2 = Regs.k1;
        Regs target = MipsInfo.value2reg.getOrDefault(this.name, Regs.k0);

        reg1 = loadValue(lhs, reg1);

        HashSet<Op> ops = new HashSet<>(){{
            add(Op.ADD); add(Op.SUB); add(Op.SDIV); // add(Op.SREM);
        }};
        if (rhs instanceof ConstInt && ops.contains(op)) {
            int val = ((ConstInt) rhs).value;
            switch (op) {
                case ADD: writeln(String.format("    addiu $%s, $%s, %d", target, reg1, val)); break;
                case SUB: writeln(String.format("    addiu $%s, $%s, %d", target, reg1, -val)); break;
                case SDIV: do_div(target, reg1, val); break;
                case SREM:
                    do_div(Regs.k1, reg1, val);
                    writeln(String.format("    mul $k1, $k1, $%s", reg1));
                    writeln(String.format("    subu $%s, $%s, $k1", target, reg1)); break;
            }
        } else {
            reg2 = loadValue(rhs, reg2);
            switch (op) {
                case ADD: writeln(String.format("    addu $%s, $%s, $%s", target, reg1, reg2)); break;
                case SUB: writeln(String.format("    subu $%s, $%s, $%s", target, reg1, reg2)); break;
                case MUL: writeln(String.format("    mul $%s, $%s, $%s", target, reg1, reg2)); break;
                case SDIV:
                    writeln(String.format("    div $%s, $%s", reg1, reg2));
                    writeln(String.format("    mflo $%s", target));
                    break;
                case SREM:
                    writeln(String.format("    div $%s, $%s", reg1, reg2));
                    writeln(String.format("    mfhi $%s", target));
                    break;
                case AND: writeln(String.format("    and $%s, $%s, $%s", target, reg1, reg2)); break;
                case OR: writeln(String.format("    or $%s, $%s, $%s", target, reg1, reg2)); break;
                case XOR: writeln(String.format("    xor $%s, $%s, $%s", target, reg1, reg2)); break;
                case SLL: writeln(String.format("    sllv $%s, $%s, $%s", target, reg1, reg2)); break;
                case SRL: writeln(String.format("    srlv $%s, $%s, $%s", target, reg1, reg2)); break;
                case SRA: writeln(String.format("    srav $%s, $%s, $%s", target, reg1, reg2)); break;
                case MULSH:
                    writeln(String.format("    mult $%s, $%s", reg1, reg2));
                    writeln(String.format("    mfhi $%s", target));
                    break;
            }
        }

        storeValue(this, target);
    }
}
