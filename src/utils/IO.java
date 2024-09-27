package utils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class IO {
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
            return null;
        }
    }

    public static void write(String filename, String content) {
        try {
            Files.write(Paths.get(filename), content.getBytes(), StandardOpenOption.APPEND);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}
