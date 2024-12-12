package utils;

import analyse.Analyse;
import ast.AstNode;
import config.Config;
import ir.Program;
import token.Token;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class IO {
    public static Path outputFile;

    public static void rmFile(String filename) {
        try {
            Files.deleteIfExists(Paths.get(filename));
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public static String read(String filename) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(filename));
            return String.join("\n", lines);
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void setOut(String filename) {
        outputFile = Paths.get(filename);
        try {
            Files.write(outputFile, "".getBytes());
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public static void write(String content) {
        try {
            Files.write(outputFile, content.getBytes(), StandardOpenOption.APPEND);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public static String last = "";
    public static void writeln(String content) {
        if (Config.taskType == Config.TaskType.MIPS
                && ((last.startsWith("    sw") && content.startsWith("    lw"))
                    || (last.startsWith("    sb") && content.startsWith("    lb")))
                && last.substring(6).equals(content.substring(6))) {
            return;
        }
        if (!content.startsWith("    #")) {
            last = content;
        }
        try {
            Files.write(outputFile, (content + '\n').getBytes(), StandardOpenOption.APPEND);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    // Output Method

    public static void initAllFiles() {
//        setOut(Config.lexerOutputFile);
//        setOut(Config.parserOutputFile);
//        setOut(Config.symbolOutputFile);
//        setOut(Config.llvmOutputFile);
//        setOut(Config.errorFile);
    }

    public static void printTokens(ArrayList<Token> tokens) {
        setOut(Config.lexerOutputFile);
        for(Token token : tokens) {
            writeln(token.toString());
        }
    }

    public static void printAst(AstNode root) {
        setOut(Config.parserOutputFile);
        root.print();
    }

    public static ArrayList<Analyse.Symbol> symbols = new ArrayList<>();
    public static void addSymbol(Analyse.Symbol symbol) {
        symbols.add(symbol);
    }
    public static void printSymbols() {
        symbols.sort(Comparator.comparingInt(s -> s.scope_id));
        setOut(Config.symbolOutputFile);
        for (Analyse.Symbol sym : symbols) {
            writeln(String.format("%d %s %s", sym.scope_id, sym.name, sym.type));
        }
    }

    public static void printIr(Program program) {
        setOut(Config.llvmOutputFile);
        program.to_llvm();
    }
}
