package utils;

import ir.type.ArrayType;
import ir.type.PointerType;
import ir.type.Type;
import token.TokenType;

import static ir.type.IntegerType.*;

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

    public static Type getType(TokenType tokenType, boolean is_array, int size) {
        return switch (tokenType) {
            case INTTK -> is_array ? (size == -1 ? new PointerType(INT_TYPE) : new ArrayType(size, INT_TYPE)) : INT_TYPE;
            case CHARTK -> is_array ? (size == -1 ? new PointerType(CHAR_TYPE) : new ArrayType(size, CHAR_TYPE)) : CHAR_TYPE;
            case VOIDTK -> VOID_TYPE;
            default -> null;
        };
    }
}
