package opt;

import ir.Program;

import static utils.IO.setOut;

public class Opt {
    public static void run(Program program) {
//        setOut("files/opt0.txt");
//        program.to_llvm();
        Mem2Reg.run(program);
//        setOut("files/opt1.txt");
//        program.to_llvm();
        RemovePhi.run(program);
//        setOut("files/opt2.txt");
//        program.to_llvm();
    }
}
