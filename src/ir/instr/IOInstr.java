package ir.instr;

import ir.ConstString;
import ir.Value;
import ir.type.PointerType;
import ir.type.Type;
import mipsGen.Regs;
import mipsGen.MipsInfo;

import static ir.type.IntegerType.*;
import static mipsGen.MipsInfo.*;
import static utils.IO.writeln;

public class IOInstr extends Instr {
    public IOInstr(Type type, String name, Value... operands) {
        super(type, name, operands);
    }

    public static class GetInt extends IOInstr {
        public GetInt(String name) {
            super(INT_TYPE, name);
        }

        @Override
        public String toString() {
            return String.format("  %s = call i32 @getint()", name);
        }

        @Override
        public void to_mips() {
            super.to_mips();
            writeln("    li $v0, 5");
            writeln("    syscall");
            storeValue(this, Regs.v0);
        }
    }

    public static class GetChar extends IOInstr {
        public GetChar(String name) {
            super(INT_TYPE, name);
        }

        @Override
        public String toString() {
            return String.format("  %s = call i32 @getchar()", name);
        }

        @Override
        public void to_mips() {
            super.to_mips();
            writeln("    li $v0, 12");
            writeln("    syscall");
            storeValue(this, Regs.v0);
        }
    }

    public static class PutInt extends IOInstr {
        public PutInt(String name, Value val) {
            super(VOID_TYPE, name, val);
            parentBB.parentFunc.hasPrint = true;
        }

        @Override
        public String toString() {
            return String.format("  call void @putint(i32 %s)", operands.get(0).name);
        }

        @Override
        public void to_mips() {
            super.to_mips();
            Value val = operands.get(0);
            Regs reg = loadValue(val, Regs.a0);
            move(Regs.a0, reg);
            writeln("    li $v0, 1");
            writeln("    syscall");
        }
    }

    public static class PutChar extends IOInstr {
        public PutChar(String name, Value val) {
            super(VOID_TYPE, name, val);
            parentBB.parentFunc.hasPrint = true;
        }

        @Override
        public String toString() {
            return String.format("  call void @putch(i8 %s)", operands.get(0).name);
        }

        @Override
        public void to_mips() {
            super.to_mips();
            Value val = operands.get(0);
            Regs reg = loadValue(val, Regs.a0);
            move(Regs.a0, reg);
            writeln("    li $v0, 11");
            writeln("    syscall");
        }
    }

    public static class PutString extends IOInstr {
        public ConstString str;

        public PutString(String name, ConstString str) {
            super(VOID_TYPE, name);
            this.str = str;
            parentBB.parentFunc.hasPrint = true;
        }

        @Override
        public String toString() {
            // getelementptr in call
            return String.format("  call void @putstr(i8* getelementptr inbounds (%s, %s %s, i32 0, i32 0))",
                    ((PointerType) str.type).elementType, str.type, str.name);
        }

        @Override
        public void to_mips() {
            super.to_mips();
            writeln(String.format("    la $a0, %s", str.name.substring(1)));
            writeln("    li $v0, 4");
            writeln("    syscall");
        }
    }
}
