package mipsGen;

import ir.ConstInt;
import ir.Function;
import ir.GlobalVariable;
import ir.Value;
import ir.type.Type;

import java.util.HashMap;

import static utils.IO.writeln;

public class mipsInfo {
    public static int cur_offset = 0; // negative
    public static HashMap<Value, Integer> value2offset = new HashMap<>();
    public static HashMap<Value, Regs> value2reg = new HashMap<>();

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
        if (value instanceof ConstInt) {
            writeln(String.format("    li $%s, %d", reg, ((ConstInt) value).value));
        } else if (value2reg.containsKey(value)) {
            reg = value2reg.get(value);
        } else {
            if (!value2offset.containsKey(value)) {
                alloc(value.type);
                value2offset.put(value, cur_offset);
            }
            if (value.type.getByte() == 4) {
                writeln(String.format("    lw $%s, %d($sp)", reg, value2offset.get(value)));
            } else {
                writeln(String.format("    lb $%s, %d($sp)", reg, value2offset.get(value)));
            }
        }
        return reg;
    }

    public static Regs loadAddress(Value value, Regs reg) {
        if (value instanceof GlobalVariable) {
            writeln(String.format("    la $%s, %s", reg, value.name.substring(1)));
        } else if (value2reg.containsKey(value)) {
            reg = value2reg.get(value);
        } else {
            if (!value2offset.containsKey(value)) {
                alloc(value.type);
                value2offset.put(value, cur_offset);
            }
            if (value.type.getByte() == 4) {
                writeln(String.format("    lw $%s, %d($sp)", reg, value2offset.get(value)));
            } else {
                writeln(String.format("    lb $%s, %d($sp)", reg, value2offset.get(value)));
            }
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
}
