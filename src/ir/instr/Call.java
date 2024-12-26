package ir.instr;

import config.Config;
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
import static mipsGen.MipsInfo.*;
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

    public void to_mips_no_opt() {
        Function targetFunc = getFunction();
        MipsInfo.alignTo(4);
        cur_offset -= 4;
        writeln(String.format("    sw $sp, %d($sp)", cur_offset));
        cur_offset -= 4;
        writeln(String.format("    sw $ra, %d($sp)", cur_offset));
        int offset = cur_offset;
        for (int i = 1; i < operands.size(); i++) {
            Value value = operands.get(i);
            FuncParam param = targetFunc.params.get(i - 1);
            loadValue(value, Regs.k0);
            alloc(param.type);
            store(param.type, Regs.k0, cur_offset, Regs.sp);
        }
        cur_offset = offset;
        writeln(String.format("    addi $sp, $sp, %d", cur_offset));
        writeln(String.format("    jal %s", targetFunc.name));
        writeln("    lw $ra, 0($sp)");
        writeln("    lw $sp, 4($sp)");
        if (targetFunc.retType == VOID_TYPE) return;
        storeValue(this, Regs.v0);
    }

    @Override
    public void to_mips() {
        super.to_mips();
        if (!Config.opt) {
            to_mips_no_opt();
            return;
        }
        // store regs
        HashSet<Regs> liveRegs = new HashSet<>();
        for (String value : this.act_out) {
            if (value2reg.containsKey(value)) {
                liveRegs.add(value2reg.get(value));
            }
        }
        for (String value : this.act_in) {
            if (value2reg.containsKey(value)) {
                liveRegs.add(value2reg.get(value));
            }
        }
        if (!MipsInfo.act_flag) liveRegs = new HashSet<>(value2reg.values());
        ArrayList<Regs> usedRegs = new ArrayList<>();
        Function func = getFunction();
        for (Regs reg : liveRegs) {
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
        HashSet<Regs> modified = new HashSet<>();
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
                    if (param instanceof FuncParam && modified.contains(value2reg.get(param.name))) {
                        writeln(String.format("    lw $%s, %d($sp)", reg, reg2off.get(src)));
                    } else {
                        move(reg, src);
                    }
                } else {
                    MipsInfo.load(param.type, reg, MipsInfo.value2offset.get(param.name), Regs.sp);
                }
                modified.add(reg);
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
        storeValue(this, Regs.v0);
    }
}
