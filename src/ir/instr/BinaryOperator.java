package ir.instr;

import ir.Value;

import static ir.type.IntegerType.INT_TYPE;
import static utils.IO.writeln;

public class BinaryOperator extends Instr {
    public enum Op {
        ADD, SUB, MUL, SDIV, SREM, SHL, SHR, AND, OR, XOR
    }

    public Op op;

    public BinaryOperator(String name, Op op, Value lhs, Value rhs) {
        super(INT_TYPE, name, lhs, rhs);
        this.op = op;
    }

    @Override
    public void print() {
        // %name = op_lower i32 %lhs, %rhs
        Value lhs = operands.get(0);
        Value rhs = operands.get(1);
        writeln(String.format("  %s = %s i32 %s, %s", name, op.toString().toLowerCase(), lhs.name, rhs.name));
    }
}
