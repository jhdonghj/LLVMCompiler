package config;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Config {
    public enum TaskType {
        LEXER, PARSER, SYMBOL, IR, MIPS
    }

    public static String inputFile = "testfile.txt";
    public static String errorFile = "error.txt";

    public static String lexerOutputFile = "lexer.txt";
    public static String parserOutputFile = "parser.txt";
    public static String symbolOutputFile = "symbol.txt";
    public static String llvmOutputFile = "llvm_ir.txt";
    public static String mipsOutputFile = "mips.txt";

    public static TaskType taskType = TaskType.MIPS;

    public static void initialize() {
        if (Files.exists(Paths.get(inputFile))) {
            return;
        }
        inputFile = "files/" + inputFile;
        errorFile = "files/" + errorFile;
        lexerOutputFile = "files/" + lexerOutputFile;
        parserOutputFile = "files/" + parserOutputFile;
        symbolOutputFile = "files/" + symbolOutputFile;
        llvmOutputFile = "files/" + llvmOutputFile;
        mipsOutputFile = "files/" + mipsOutputFile;
    }
}
