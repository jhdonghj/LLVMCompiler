package mipsGen;

import ir.*;
import ir.type.Type;

import java.util.HashMap;

import static utils.IO.writeln;

public class MipsInfo {
    public static int cur_offset = 0; // negative
    public static HashMap<String, Integer> value2offset = new HashMap<>();
    public static HashMap<String, Regs> value2reg = new HashMap<>();
    public static boolean act_flag = false;

    public static void enter(Function func) {
        cur_offset = 0;
        value2offset.clear();
        value2reg.clear();
    }

    public static void alignTo(int align) {
        cur_offset = Math.floorDiv(cur_offset, align) * align;
    }

    public static void alloc(Type type) {
        int size = type.getSize(), align = type.getAlign();
        alignTo(align);
        cur_offset -= size;
    }

    public static Regs loadValue(Value value, Regs reg) {
        // load value from object to reg
        if (value instanceof ConstInt) {
            writeln(String.format("    li $%s, %d", reg, ((ConstInt) value).value));
        } else if (value2reg.containsKey(value.name)) {
            reg = value2reg.get(value.name);
        } else {
            if (!value2offset.containsKey(value.name)) {
                alloc(value.type);
                value2offset.put(value.name, cur_offset);
            }
            load(value.type, reg, value2offset.get(value.name), Regs.sp);
        }
        return reg;
    }

    public static void storeValue(Value value, Regs reg) {
        // store value from reg to object
        if (value2reg.containsKey(value.name)) {
            move(value2reg.get(value.name), reg);
        } else {
            if (!value2offset.containsKey(value.name)) {
                alloc(value.type);
                value2offset.put(value.name, cur_offset);
            }
            store(value.type, reg, value2offset.get(value.name), Regs.sp);
        }
    }

    public static Regs loadAddress(Value value, Regs reg) {
        if (value instanceof GlobalVariable) {
            writeln(String.format("    la $%s, %s", reg, value.name.substring(1)));
        } else if (value2reg.containsKey(value.name)) {
            reg = value2reg.get(value.name);
        } else {
            if (!value2offset.containsKey(value.name)) {
                alloc(value.type);
                value2offset.put(value.name, cur_offset);
            }
            load(value.type, reg, value2offset.get(value.name), Regs.sp);
        }
        return reg;
    }

    public static void load(Type type, Regs target_reg, int offset, Regs pointer_reg) {
        if (type.getByte() == 4) {
            writeln(String.format("    lw $%s, %d($%s)", target_reg, offset, pointer_reg));
        } else {
            writeln(String.format("    lb $%s, %d($%s)", target_reg, offset, pointer_reg));
        }
    }

    public static void store(Type type, Regs target_reg, int offset, Regs pointer_reg) {
        if (type.getByte() == 4) {
            writeln(String.format("    sw $%s, %d($%s)", target_reg, offset, pointer_reg));
        } else {
            writeln(String.format("    sb $%s, %d($%s)", target_reg, offset, pointer_reg));
        }
    }

    public static void move(Regs reg1, Regs reg2) {
        if (!reg1.equals(reg2)) {
            writeln(String.format("    move $%s, $%s", reg1, reg2));
        }
    }
}
