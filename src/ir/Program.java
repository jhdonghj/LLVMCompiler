package ir;

import java.util.ArrayList;

public class Program {
    public ArrayList<GlobalVariable> globalVariables;
    public ArrayList<ConstString> constStrings;
    public ArrayList<Function> functions;

    public Program() {
        globalVariables = new ArrayList<>();
        constStrings = new ArrayList<>();
        functions = new ArrayList<>();
    }
}
