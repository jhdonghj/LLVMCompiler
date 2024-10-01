package utils;

import token.TokenType;

public class Utils {
    public static TokenType singleType(char c) {
        return switch (c) {
            case '(' -> TokenType.LPARENT;
            case ')' -> TokenType.RPARENT;
            case '[' -> TokenType.LBRACK;
            case ']' -> TokenType.RBRACK;
            case '{' -> TokenType.LBRACE;
            case '}' -> TokenType.RBRACE;
            case ',' -> TokenType.COMMA;
            case ';' -> TokenType.SEMICN;
            case '=' -> TokenType.ASSIGN;
            case '+' -> TokenType.PLUS;
            case '-' -> TokenType.MINU;
            case '*' -> TokenType.MULT;
            case '/' -> TokenType.DIV;
            case '%' -> TokenType.MOD;
            case '<' -> TokenType.LSS;
            case '>' -> TokenType.GRE;
            case '!' -> TokenType.NOT;
            default -> null;
        };
    }
}
