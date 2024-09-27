package err;

import config.Config;
import utils.IO;

import java.util.ArrayList;

public class ErrHandler {
    private static ArrayList<ErrInfo> errors = new ArrayList<ErrInfo>();

    public static void addError(ErrInfo error) {
        errors.add(error);
    }

    public static boolean hasErr() {
        return !errors.isEmpty();
    }

    public static void printErrs() {
        IO.clearFile(Config.errorFile);
        for (ErrInfo err : errors) {
            IO.write(Config.errorFile, err.toString() + '\n');
        }
    }
}
