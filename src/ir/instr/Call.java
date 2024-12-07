package ir.instr;

import ir.ConstInt;
import ir.Function;
import ir.Value;
import mipsGen.Regs;
import mipsGen.mipsInfo;

import java.util.ArrayList;
import java.util.HashSet;

import static ir.type.IntegerType.VOID_TYPE;
import static utils.IO.writeln;

public class Call extends Instr {
    public Call(String name, Function func, Value... args) {
        super(func.retType, name, func);
        addOperands(args);
    }

    public Call(String name, Function func, ArrayList<Value> args) {
        super(func.retType, name, func);
        for (Value arg : args) {
            addOperands(arg);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("  ");
        if (type != VOID_TYPE) {
            sb.append(name).append(" = ");
        }
        sb.append("call ").append(type).append(" @").append(operands.get(0).name).append("(");
        for (int i = 1; i < operands.size(); i++) {
            if (i > 1) {
                sb.append(", ");
            }
            sb.append(operands.get(i).type).append(" ").append(operands.get(i).name);
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public void to_mips() {
        super.to_mips();
        // store regs
        ArrayList usedRegs = new ArrayList<>(new HashSet<>(mipsInfo.value2reg.values()));
        mipsInfo.alignTo(4);
        int offset = mipsInfo.cur_offset;
        for (int i = 0; i < usedRegs.size(); i++) {
            offset -= 4;
            writeln(String.format("    sw $%s, %d($sp)", usedRegs.get(i), offset));
        }
        offset -= 4;
        writeln(String.format("    sw $sp, %d($sp)", offset));
        offset -= 4;
        writeln(String.format("    sw $ra, %d($sp)", offset));
        // store params
        for (int i = 1; i < operands.size(); i++) {
            Value param = operands.get(i);
            if (i <= 4) {
                Regs reg = Regs.a0.get(i - 1);
                if (param instanceof ConstInt) {
                    writeln(String.format("    li $%s, %d", reg, ((ConstInt) param).value));
                } else if (mipsInfo.value2reg.containsKey(param)) {
                    // to do
                } else {
                    mipsInfo.load(param.type, reg, mipsInfo.value2offset.get(param), Regs.sp);
                }
            } else {
                Regs reg = Regs.k0;
                if (param instanceof ConstInt) {
                    writeln(String.format("    li $%s, %d", reg, ((ConstInt) param).value));
                } else if (mipsInfo.value2reg.containsKey(param)) {
                    // to do
                } else {
                    mipsInfo.load(param.type, reg, mipsInfo.value2offset.get(param), Regs.sp);
                }
                writeln(String.format("    sw $%s, %d($sp)", reg, offset - 4 * i));
            }
        }
        // call
        Function func = (Function) operands.get(0);
        writeln(String.format("    addi $sp, $sp, %d", offset));
        writeln(String.format("    jal %s", func.name));
        writeln("    lw $ra, 0($sp)");
        writeln("    lw $sp, 4($sp)");
        // restore regs
        offset += 8;
        for (int i = usedRegs.size() - 1; i >= 0; i--) {
            writeln(String.format("    lw $%s, %d($sp)", usedRegs.get(i), offset));
            offset += 4;
        }
        if (func.retType == VOID_TYPE) return;
        // handle return value
        if (mipsInfo.value2reg.containsKey(this)) {
            // to do
        } else {
            mipsInfo.alloc(func.retType);
            mipsInfo.value2offset.put(this, mipsInfo.cur_offset);
            mipsInfo.store(func.retType, Regs.v0, mipsInfo.value2offset.get(this), Regs.sp);
        }
    }
}
