package err;

import config.Config;
import utils.IO;

import java.util.ArrayList;

public class ErrHandler {
    private static ArrayList<ErrInfo> errors = new ArrayList<ErrInfo>();
    private static boolean isOpen = true;

    public static void close() {
        isOpen = false;
    }

    public static void open() {
        isOpen = true;
    }

    public static void addError(ErrInfo error) {
        if (isOpen) {
            errors.add(error);
        }
    }

    public static boolean hasErr() {
        return !errors.isEmpty();
    }

    public static void printErrs() {
        errors.sort(null);
        IO.setOut(Config.errorFile);
        for (ErrInfo err : errors) {
            IO.writeln(err.toString());
        }
    }
}
