package opt;

import ir.Program;

import static utils.IO.setOut;

public class Opt {
    public static void run(Program program) {
//        setOut("files/opt0.txt");
//        program.to_llvm();
//        System.out.println("Mem2Reg");
        Mem2Reg.run(program);
//        System.out.println("DCE");
        DCE.run(program);
//        setOut("files/opt2.txt");
//        program.to_llvm();
//        System.out.println("RemovePhi");
        RemovePhi.run(program);
//        System.out.println("ReOrderBB");
        ReOrderBB.run(program);
//        setOut("files/opt1.txt");
//        program.to_llvm();
//        System.out.println("RegAlloc");
        RegAlloc.run(program);
//        System.out.println("FuncRegClosure");
        FuncRegClosure.run(program);
//        System.out.println("Opt End");
//        setOut("files/opt2.txt");
//        program.to_llvm();
    }
}
