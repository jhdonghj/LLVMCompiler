package ir;

import java.util.ArrayList;

import static utils.IO.writeln;

public class Program {
    public ArrayList<GlobalVariable> globalVariables;
    public ArrayList<ConstString> constStrings;
    public ArrayList<Function> functions;

    public Program() {
        globalVariables = new ArrayList<>();
        constStrings = new ArrayList<>();
        functions = new ArrayList<>();
    }

    public void addGlobalVariable(GlobalVariable globalVariable) {
        globalVariables.add(globalVariable);
    }

    public void addConstString(ConstString constString) {
        constStrings.add(constString);
    }

    public void addFunction(Function function) {
        functions.add(function);
    }

    public void to_llvm() {
        writeln("declare i32 @getint()");
        writeln("declare i32 @getchar()");
        writeln("declare void @putint(i32)");
        writeln("declare void @putch(i8)");
        writeln("declare void @putstr(i8*)\n");
        for (ConstString constString : constStrings) {
            constString.to_llvm();
        }
        writeln("");;
        for (GlobalVariable globalVariable : globalVariables) {
            globalVariable.to_llvm();
        }
        writeln("");
        for (Function function : functions) {
            function.to_llvm();
            writeln("");
        }
    }

    public void to_mips() {
        writeln(".data");
        for (GlobalVariable globalVariable : globalVariables) {
            globalVariable.to_mips();
        }
        for (ConstString constString : constStrings) {
            constString.to_mips();
        }
        writeln(".text");
        writeln("# jump to main");
        writeln("    jal main");
        writeln("    j _exit");
        writeln("");
        for (Function function : functions) {
            function.to_mips();
        }
        writeln("_exit:");
    }
}
