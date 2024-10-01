import ast.AstNode;
import astGen.Lexer;
import astGen.Parser;
import config.Config;
import err.ErrHandler;
import token.Token;
import utils.IO;

import java.util.ArrayList;

public class Compiler {
    public static void main(String[] args) {
        Config.initialize();
        IO.initAllFiles();
        ArrayList<Token> tokens = Lexer.lex(IO.read(Config.inputFile));
        if (Config.taskType == Config.TaskType.LEXER && !ErrHandler.hasErr()) {
            IO.printTokens(tokens);
        }
        AstNode root = Parser.parse(tokens);
        if (Config.taskType == Config.TaskType.PARSER && !ErrHandler.hasErr()) {
            IO.printAst(root);
        }
        
        if (ErrHandler.hasErr()) {
            ErrHandler.printErrs();
        }
    }
}
