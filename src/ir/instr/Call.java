package ir.instr;

import ir.ConstInt;
import ir.FuncParam;
import ir.Function;
import ir.Value;
import mipsGen.Regs;
import mipsGen.MipsInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static ir.type.IntegerType.VOID_TYPE;
import static mipsGen.MipsInfo.move;
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

    public Function getFunction() {
        return (Function) operands.get(0);
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
        ArrayList<Regs> usedRegs = new ArrayList<>();
        Function func = getFunction();
        for (Regs reg : MipsInfo.value2reg.values()) {
            if (func.regClosure.contains(reg) ||
                    (Regs.a0.ordinal() <= reg.ordinal() && reg.ordinal() <= Regs.a3.ordinal())) {
                usedRegs.add(reg);
            }
        }
        HashMap<Regs, Integer> reg2off = new HashMap<>();
        MipsInfo.alignTo(4);
        int offset = MipsInfo.cur_offset;
        for (Regs usedReg : usedRegs) {
            offset -= 4;
            writeln(String.format("    sw $%s, %d($sp)", usedReg, offset));
            reg2off.put(usedReg, offset);
        }
        offset -= 4;
        writeln(String.format("    sw $sp, %d($sp)", offset));
        offset -= 4;
        writeln(String.format("    sw $ra, %d($sp)", offset));
        // store params
        for (int i = 1; i < operands.size(); i++) {
            Value param = operands.get(i);
            if (i <= 3 || (!func.hasPrint && i <= 4)) {
                Regs reg;
                if (func.hasPrint) {
                    reg = Regs.a1.get(i - 1);
                } else {
                    reg = Regs.a0.get(i - 1);
                }
                if (param instanceof ConstInt) {
                    writeln(String.format("    li $%s, %d", reg, ((ConstInt) param).value));
                } else if (MipsInfo.value2reg.containsKey(param.name)) {
                    Regs src = MipsInfo.value2reg.get(param.name);
                    if (param instanceof FuncParam) {
                        writeln(String.format("    lw $%s, %d($sp)", reg, reg2off.get(src)));
                    } else {
                        writeln(String.format("    move $%s, $%s", reg, src));
                    }
                } else {
                    MipsInfo.load(param.type, reg, MipsInfo.value2offset.get(param.name), Regs.sp);
                }
            } else {
                Regs reg = Regs.k0;
                if (param instanceof ConstInt) {
                    writeln(String.format("    li $%s, %d", reg, ((ConstInt) param).value));
                } else if (MipsInfo.value2reg.containsKey(param.name)) {
                    Regs src = MipsInfo.value2reg.get(param.name);
                    if (param instanceof FuncParam) {
                        writeln(String.format("    lw $%s, %d($sp)", reg, reg2off.get(src)));
                    } else {
                        reg = src;
                    }
                } else {
                    MipsInfo.load(param.type, reg, MipsInfo.value2offset.get(param.name), Regs.sp);
                }
                writeln(String.format("    sw $%s, %d($sp)", reg, offset - 4 * i));
            }
        }
        // call
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
        if (MipsInfo.value2reg.containsKey(this.name)) {
            move(MipsInfo.value2reg.get(this.name), Regs.v0);
        } else {
            MipsInfo.alloc(func.retType);
            MipsInfo.value2offset.put(this.name, MipsInfo.cur_offset);
            MipsInfo.store(func.retType, Regs.v0, MipsInfo.value2offset.get(this.name), Regs.sp);
        }
    }
}
