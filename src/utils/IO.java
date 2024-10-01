package utils;

import ast.AstNode;
import config.Config;
import token.Token;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class IO {
    public static Path outputFile;

    public static void clearFile(String filename) {
        try {
            Files.write(Paths.get(filename), "".getBytes());
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

    public static void writeln(String content) {
        try {
            Files.write(outputFile, (content + '\n').getBytes(), StandardOpenOption.APPEND);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    // Output Method

    public static void initAllFiles() {
        setOut(Config.lexerOutputFile);
        setOut(Config.parserOutputFile);
        setOut(Config.errorFile);
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
}
