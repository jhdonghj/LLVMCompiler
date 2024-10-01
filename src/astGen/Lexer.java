package astGen;

import err.*;
import token.*;

import java.util.ArrayList;
import java.util.HashMap;

public class Lexer {
    public static HashMap<String, TokenType> reserveWords = new HashMap<>() {{
        put("main", TokenType.MAINTK);
        put("const", TokenType.CONSTTK);
        put("int", TokenType.INTTK);
        put("char", TokenType.CHARTK);
        put("break", TokenType.BREAKTK);
        put("continue", TokenType.CONTINUETK);
        put("if", TokenType.IFTK);
        put("else", TokenType.ELSETK);
        put("for", TokenType.FORTK);
        put("getint", TokenType.GETINTTK);
        put("getchar", TokenType.GETCHARTK);
        put("printf", TokenType.PRINTFTK);
        put("return", TokenType.RETURNTK);
        put("void", TokenType.VOIDTK);
    }};
    public static HashMap<Character, TokenType> single = new HashMap<>() {{
        put('+', TokenType.PLUS);
        put('-', TokenType.MINU);
        put('*', TokenType.MULT);
        put('%', TokenType.MOD);
        put(';', TokenType.SEMICN);
        put(',', TokenType.COMMA);
        put('(', TokenType.LPARENT);
        put(')', TokenType.RPARENT);
        put('[', TokenType.LBRACK);
        put(']', TokenType.RBRACK);
        put('{', TokenType.LBRACE);
        put('}', TokenType.RBRACE);
    }};

    public static ArrayList<Token> lex(String input) {
        ArrayList<Token> tokens = new ArrayList<>();
        int atLine = 1, i = 0;
        boolean lineComment = false, blockComment = false;
        while(i < input.length()) {
            char c = input.charAt(i);
            char nc = i + 1 < input.length() ? input.charAt(i + 1) : 0;
            if(c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                if (c == '\n') {
                    atLine++;
                    lineComment = false;
                }
                i++;
            } else if (blockComment) {
                if (c == '*' && nc == '/') {
                    blockComment = false;
                    i += 2;
                } else {
                    i++;
                }
            } else if (lineComment) {
                i++;
            } else if (c == '/') {
                if (nc == '/') {
                    lineComment = true;
                    i += 2;
                } else if (nc == '*') {
                    blockComment = true;
                    i += 2;
                } else {
                    tokens.add(new Token(TokenType.DIV, "/", atLine));
                    i++;
                }
            } else if (single.containsKey(c)) {
                tokens.add(new Token(single.get(c), String.valueOf(c), atLine));
                i++;
            } else if (c == '=') {
                if(nc == '=') {
                    tokens.add(new Token(TokenType.EQL, "==", atLine));
                    i += 2;
                } else {
                    tokens.add(new Token(TokenType.ASSIGN, "=", atLine));
                    i++;
                }
            } else if (c == '!') {
                if(nc == '=') {
                    tokens.add(new Token(TokenType.NEQ, "!=", atLine));
                    i += 2;
                } else {
                    tokens.add(new Token(TokenType.NOT, "!", atLine));
                    i++;
                }
            } else if (c == '<') {
                if(nc == '=') {
                    tokens.add(new Token(TokenType.LEQ, "<=", atLine));
                    i += 2;
                } else {
                    tokens.add(new Token(TokenType.LSS, "<", atLine));
                    i++;
                }
            } else if (c == '>') {
                if(nc == '=') {
                    tokens.add(new Token(TokenType.GEQ, ">=", atLine));
                    i += 2;
                } else {
                    tokens.add(new Token(TokenType.GRE, ">", atLine));
                    i++;
                }
            } else if (c == '|') {
                if (nc == '|') {
                    tokens.add(new Token(TokenType.OR, "||", atLine));
                    i += 2;
                } else {
                    ErrHandler.addError(new ErrInfo(ErrType.a, atLine));
                    tokens.add(new Token(TokenType.OR, "|", atLine));
                    i++;
                }
            } else if (c == '&') {
                if (nc == '&') {
                    tokens.add(new Token(TokenType.AND, "&&", atLine));
                    i += 2;
                } else {
                    ErrHandler.addError(new ErrInfo(ErrType.a, atLine));
                    tokens.add(new Token(TokenType.AND, "&", atLine));
                    i++;
                }
            } else if (c == '\'') { // recognize char const
                if (nc == '\\') {
                    tokens.add(new Token(TokenType.CHRCON, input.substring(i, i + 4), atLine));
                    i += 4;
                } else {
                    tokens.add(new Token(TokenType.CHRCON, input.substring(i, i + 3), atLine));
                    i += 3;
                }
            } else if (c == '\"') {
                int j = i;
                boolean slash = false;
                i++;
                while(i < input.length() && (input.charAt(i) != '\"' || slash)) {
                    if (input.charAt(i) == '\\') {
                        slash = !slash;
                    } else {
                        slash = false;
                    }
                    i++;
                }
                i++;
                tokens.add(new Token(TokenType.STRCON, input.substring(j, i), atLine));
            } else if (c == '_' || Character.isLetter(c)) { // recognize ident
                int j = i;
                boolean slash = false;
                while(i < input.length() && (Character.isLetterOrDigit(input.charAt(i)) || input.charAt(i) == '_')) {
                    i++;
                }
                String ident = input.substring(j, i);
                tokens.add(new Token(reserveWords.getOrDefault(ident, TokenType.IDENFR), ident, atLine));
            } else if (Character.isDigit(c)) { // recognize int const
                int j = i;
                while(i < input.length() && Character.isDigit(input.charAt(i))) {
                    i++;
                }
                tokens.add(new Token(TokenType.INTCON, input.substring(j, i), atLine));
            } else {
                assert false;
            }
        }
        return tokens;
    }
}
