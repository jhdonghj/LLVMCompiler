package ir.instr;

import ir.ConstInt;
import ir.Value;
import ir.type.ArrayType;
import ir.type.PointerType;
import ir.type.Type;
import mipsGen.Regs;
import mipsGen.MipsInfo;
import utils.Utils;

import static mipsGen.MipsInfo.*;
import static utils.IO.writeln;

public class GetElementPointer extends Instr {
    public GetElementPointer(String name, Value pointer, Value... offsets) {
        super(null, name, pointer);
        Type type = pointer.type;
        for (Value offset : offsets) {
            addOperands(offset);
            if (type instanceof ArrayType) {
                type = ((ArrayType) type).elementType;
            } else if (type instanceof PointerType) {
                type = ((PointerType) type).elementType;
            }
        }
        this.type = new PointerType(type);
    }

    @Override
    public String toString() {
        // <result> = getelementptr <ty>, <ty>* <ptrval>{, [inrange] <ty> <idx>}*
        Value pointer = operands.get(0);
        PointerType pointerType = (PointerType) pointer.type;
        Type targetType = pointerType.elementType;
        StringBuilder sb = new StringBuilder();
        sb.append("  ").append(name).append(" = getelementptr ").append(targetType).append(", ")
                .append(pointerType).append(" ").append(pointer.name);
        for (int i = 1; i < operands.size(); i++) {
            sb.append(", ").append(operands.get(i).type).append(" ").append(operands.get(i).name);
        }
        return sb.toString();
    }

    @Override
    public void to_mips() {
        super.to_mips();
        Value pointer = operands.get(0);
        Regs pointer_reg = Regs.k0;
        Regs offset_reg = Regs.k1;
        Regs target_reg = value2reg.getOrDefault(this.name, Regs.k0);

        pointer_reg = loadAddress(pointer, pointer_reg);
        move(Regs.k0, pointer_reg);
        pointer_reg = Regs.k0;

        Type type = pointer.type;
        for (int i = 1; i < operands.size(); i++) {
            Value offset = operands.get(i);
            if (offset instanceof ConstInt) {
                int offset_val = ((ConstInt) offset).value;
                if (offset_val != 0) {
                    writeln(String.format("    addi $%s, $%s, %d",
                            pointer_reg, pointer_reg, offset_val * type.getElementType().getSize()));
                }
            } else {
                offset_reg = loadValue(offset, offset_reg);
                int size = type.getElementType().getSize();
                if (Utils.popcount(size) == 1) {
                    writeln(String.format("    sll $k1, $%s, %d", offset_reg, Utils.ctz(size)));
                } else {
                    writeln(String.format("    mul $k1, $%s, %d", offset_reg, size));
                }
                writeln(String.format("    add $%s, $%s, $k1", pointer_reg, pointer_reg));
            }
            type = new PointerType(type.getElementType().getElementType());
        }
        move(target_reg, pointer_reg);

//        storeValue(this, target_reg);
        if (!MipsInfo.value2reg.containsKey(this.name)) {
            MipsInfo.alloc(type);
            MipsInfo.value2offset.put(this.name, MipsInfo.cur_offset);
            writeln(String.format("    sw $%s, %d($sp)", target_reg, MipsInfo.value2offset.get(this.name)));
        }
    }
}
