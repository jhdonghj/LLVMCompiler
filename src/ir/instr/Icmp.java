package ir.instr;

import ir.Value;

import static ir.type.IntegerType.BOOL_TYPE;
import static utils.IO.writeln;

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
    public void print() {
        // %name = icmp op i32 %lhs, %rhs
        Value lhs = operands.get(0);
        Value rhs = operands.get(1);
        writeln(String.format("  %s = icmp %s i32 %s, %s", name, op.toString().toLowerCase(), lhs.name, rhs.name));
    }
}
