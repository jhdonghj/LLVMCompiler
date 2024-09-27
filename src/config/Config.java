package config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Config {
    public static String inputFile = "testfile.txt";
    public static String lexerOutputFile = "lexer.txt";
    public static String errorFile = "error.txt";

    public static boolean isPrintTokens = true;

    public static void local() {
        if (Files.exists(Paths.get(inputFile))) {
            return;
        }
        inputFile = "files/" + inputFile;
        lexerOutputFile = "files/" + lexerOutputFile;
        errorFile = "files/" + errorFile;
    }
}
