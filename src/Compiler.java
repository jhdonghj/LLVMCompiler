import astGen.Lexer;
import config.Config;
import err.ErrHandler;
import token.Token;
import utils.IO;

import java.util.ArrayList;

public class Compiler {
    public static void main(String[] args) {
        Config.local();
        String input = IO.read(Config.inputFile);
        ArrayList<Token> tokens = Lexer.lex(input);
        if (Config.isPrintTokens && !ErrHandler.hasErr()) {
            Lexer.printTokens(tokens);
        }
        
        if (ErrHandler.hasErr()) {
            ErrHandler.printErrs();
        }
    }
}
