package astGen;

import ast.*;
import err.*;
import token.*;

import java.util.ArrayList;

import static utils.Utils.singleType;

public class Parser {
    public static ArrayList<Token> tokens;
    public static int index;
    public static Token now;

    // Helper functions

    private static void next() {
        index++;
        if (index < tokens.size()) {
            now = tokens.get(index);
        } else {
            now = new Token(TokenType.EOF, "", 0);
        }
    }

    private static void pushToken(AstNode node, Token token) {
        node.add(new AstNode(token));
        next();
    }

    private static void pushToken(AstNode node, Token token, TokenType type, ErrInfo errInfo) {
        if (token.type == type) {
            pushToken(node, token);
        } else {
            ErrHandler.addError(errInfo);
            node.add(new AstNode(token));
        }
    }

    private static Token peek(int offset) {
        if (index + offset >= tokens.size() || index + offset < 0) {
            return new Token(TokenType.EOF, "", 0);
        }
        return tokens.get(index + offset);
    }

    private static boolean expFirst() {
        return now.type == TokenType.IDENFR || now.type == TokenType.INTCON || now.type == TokenType.CHRCON ||
                now.type == singleType('(') || now.type == singleType('+') || now.type == singleType('-') ||
                now.type == singleType('!');
    }

    private static AstNode form(ArrayList<AstNode> list, AstType type) {
        AstNode node = new AstNode(type, now.line);
        node.add(list.get(0));
        for (int i = 1; i < list.size(); i += 2) {
            AstNode newNode = new AstNode(type, now.line);
            newNode.add(node);
            newNode.add(list.get(i));
            newNode.add(list.get(i + 1));
            node = newNode;
        }
        return node;
    }

    // Main functions

    public static AstNode parse(ArrayList<Token> tokens) {
        Parser.tokens = tokens;
        Parser.index = 0;
        now = tokens.get(0);
        return CompUnit();
    }

    private static AstNode CompUnit() {
        AstNode node = new AstNode(AstType.CompUnit, now.line);
        while (now.type == TokenType.CONSTTK ||
                ((now.type == TokenType.INTTK || now.type == TokenType.CHARTK) &&
                 peek(1).type == TokenType.IDENFR &&
                 peek(2).type != singleType('(')
                )) {
            node.add(Decl());
        }
        while ((now.type == TokenType.VOIDTK ||
                now.type == TokenType.INTTK ||
                now.type == TokenType.CHARTK) &&
               peek(1).type == TokenType.IDENFR) {
            node.add(FuncDef());
        }
        node.add(MainFuncDef());
        return node;
    }

    private static AstNode Decl() {
        AstNode node = new AstNode(AstType.Decl, now.line);
        if (now.type == TokenType.CONSTTK) {
            node.add(ConstDecl());
        } else {
            node.add(VarDecl());
        }
        return node;
    }

    private static AstNode ConstDecl() {
        AstNode node = new AstNode(AstType.ConstDecl, now.line);
        pushToken(node, now); // const
        node.add(BType());
        node.add(ConstDef());
        while (now.type == singleType(',')) {
            pushToken(node, now);
            node.add(ConstDef());
        }
        pushToken(node, now, singleType(';'), new ErrInfo(ErrType.i, node.lstLine));
        return node;
    }

    private static AstNode BType() {
        AstNode node = new AstNode(AstType.BType, now.line);
        pushToken(node, now); // int/char
        return node;
    }

    private static AstNode ConstDef() {
        AstNode node = new AstNode(AstType.ConstDef, now.line);
        pushToken(node, now); // Ident
        if (now.type == singleType('[')) {
            pushToken(node, now); // [
            node.add(ConstExp());
            pushToken(node, now, singleType(']'), new ErrInfo(ErrType.k, node.lstLine));
        }
        pushToken(node, now); // =
        node.add(ConstInitVal());
        return node;
    }

    private static AstNode ConstInitVal() {
        AstNode node = new AstNode(AstType.ConstInitVal, now.line);
        if (now.type == singleType('{')) {
            pushToken(node, now); // {
            if (now.type != singleType('}')) {
                node.add(ConstExp());
                while (now.type == singleType(',')) {
                    pushToken(node, now); // ,
                    node.add(ConstExp());
                }
            }
            pushToken(node, now); // }
        } else if (now.type == TokenType.STRCON) {
            pushToken(node, now);
        } else {
            node.add(ConstExp());
        }
        return node;
    }

    private static AstNode VarDecl() {
        AstNode node = new AstNode(AstType.VarDecl, now.line);
        node.add(BType());
        node.add(VarDef());
        while (now.type == singleType(',')) {
            pushToken(node, now); // ,
            node.add(VarDef());
        }
        pushToken(node, now, singleType(';'), new ErrInfo(ErrType.i, node.lstLine));
        return node;
    }

    private static AstNode VarDef() {
        AstNode node = new AstNode(AstType.VarDef, now.line);
        pushToken(node, now); // Ident
        if (now.type == singleType('[')) {
            pushToken(node, now); // [
            node.add(ConstExp());
            pushToken(node, now, singleType(']'), new ErrInfo(ErrType.k, node.lstLine));
        }
        if (now.type == singleType('=')) {
            pushToken(node, now); // =
            node.add(InitVal());
        }
        return node;
    }

    private static AstNode InitVal() {
        AstNode node = new AstNode(AstType.InitVal, now.line);
        if (now.type == singleType('{')) {
            pushToken(node, now);
            if (now.type != singleType('}')) {
                node.add(Exp());
                while (now.type == singleType(',')) {
                    pushToken(node, now);
                    node.add(Exp());
                }
            }
            pushToken(node, now);
        } else if (now.type == TokenType.STRCON) {
            pushToken(node, now);
        } else {
            node.add(Exp());
        }
        return node;
    }

    private static AstNode FuncDef() {
        AstNode node = new AstNode(AstType.FuncDef, now.line);
        node.add(FuncType());
        pushToken(node, now); // Ident
        pushToken(node, now); // (
        if (now.type == TokenType.INTTK || now.type == TokenType.CHARTK) {
            node.add(FuncFParams());
        }
        pushToken(node, now, singleType(')'), new ErrInfo(ErrType.j, node.lstLine));
        node.add(Block());
        return node;
    }

    private static AstNode MainFuncDef() {
        AstNode node = new AstNode(AstType.MainFuncDef, now.line);
        pushToken(node, now); // int
        pushToken(node, now); // main
        pushToken(node, now); // (
        pushToken(node, now, singleType(')'), new ErrInfo(ErrType.j, node.lstLine));
        node.add(Block());
        return node;
    }

    private static AstNode FuncType() {
        AstNode node = new AstNode(AstType.FuncType, now.line);
        pushToken(node, now);
        return node;
    }

    private static AstNode FuncFParams() {
        AstNode node = new AstNode(AstType.FuncFParams, now.line);
        node.add(FuncFParam());
        while (now.type == singleType(',')) {
            pushToken(node, now);
            node.add(FuncFParam());
        }
        return node;
    }

    private static AstNode FuncFParam() {
        AstNode node = new AstNode(AstType.FuncFParam, now.line);
        node.add(BType());
        pushToken(node, now); // Ident
        if (now.type == singleType('[')) {
            pushToken(node, now); // [
            pushToken(node, now, singleType(']'), new ErrInfo(ErrType.k, node.lstLine));
        }
        return node;
    }

    private static AstNode Block() {
        AstNode node = new AstNode(AstType.Block, now.line);
        pushToken(node, now); // {
        while (now.type != singleType('}')) {
            node.add(BlockItem());
        }
        pushToken(node, now); // }
        return node;
    }

    private static AstNode BlockItem() {
        AstNode node = new AstNode(AstType.BlockItem, now.line);
        if (now.type == TokenType.INTTK || now.type == TokenType.CHARTK || now.type == TokenType.CONSTTK) {
            node.add(Decl());
        } else {
            node.add(Stmt());
        }
        return node;
    }

    private static AstNode Stmt() {
        AstNode node = new AstNode(AstType.Stmt, now.line);
        if (now.type == TokenType.IFTK) {
            node.subType = 1;
            pushToken(node, now); // if
            pushToken(node, now); // (
            node.add(Cond());
            pushToken(node, now, singleType(')'), new ErrInfo(ErrType.j, node.lstLine));
            node.add(Stmt());
            if (now.type == TokenType.ELSETK) {
                pushToken(node, now); // else
                node.add(Stmt());
            }
        } else if (now.type == TokenType.FORTK) {
            node.subType = 2;
            pushToken(node, now); // for
            pushToken(node, now); // (
            if (now.type != singleType(';')) {
                node.add(ForStmt());
            }
            pushToken(node, now); // ;
            if (now.type != singleType(';')) {
                node.add(Cond());
            }
            pushToken(node, now); // ;
            if (now.type != singleType(')')) {
                node.add(ForStmt());
            }
            pushToken(node, now); // )
            node.add(Stmt());
        } else if (now.type == TokenType.BREAKTK || now.type == TokenType.CONTINUETK) {
            node.subType = 3;
            pushToken(node, now);
            pushToken(node, now, singleType(';'), new ErrInfo(ErrType.i, node.lstLine));
        } else if (now.type == singleType('{')) {
            node.subType = 4;
            node.add(Block());
        } else if (now.type == TokenType.PRINTFTK) {
            node.subType = 5;
            pushToken(node, now); // printf
            pushToken(node, now); // (
            pushToken(node, now); // stringConst
            while (now.type == singleType(',')) {
                pushToken(node, now); // ,
                node.add(Exp());
            }
            pushToken(node, now, singleType(')'), new ErrInfo(ErrType.j, node.lstLine));
            pushToken(node, now, singleType(';'), new ErrInfo(ErrType.i, node.lstLine));
        } else if (now.type == TokenType.RETURNTK) {
            node.subType = 6;
            pushToken(node, now); // return
            if (expFirst()) {
                node.add(Exp());
            }
            pushToken(node, now, singleType(';'), new ErrInfo(ErrType.i, node.lstLine));
        } else if (now.type == TokenType.IDENFR && peek(1).type != singleType('(')) {
            // Exp begins with Ident in two cases:  Exp -> LVal, Ident '(' [FuncRParams] ')'
            // so if it is not the second case, we can always parse a LVal
            int lstIndex = index;
            ErrHandler.close();
            LVal(); // parse a LVal
            ErrHandler.open();
            Token nextToken = now;
            index = lstIndex;
            now = tokens.get(index);
            if (nextToken.type == singleType('=')) {
                // it is actually LVal, aad it into node
                node.add(LVal()); // rerun to make ErrHandler work
                pushToken(node, now); // =
                if (now.type == TokenType.GETINTTK || now.type == TokenType.GETCHARTK) {
                    node.subType = 8;
                    pushToken(node, now); // getInt / getChar
                    pushToken(node, now); // (
                    pushToken(node, now, singleType(')'), new ErrInfo(ErrType.j, node.lstLine));
                    pushToken(node, now, singleType(';'), new ErrInfo(ErrType.i, node.lstLine));
                } else {
                    node.subType = 9;
                    node.add(Exp());
                    pushToken(node, now, singleType(';'), new ErrInfo(ErrType.i, node.lstLine));
                }
            } else {
                // it is not LVal but Exp, go back and parse Exp
                // one LVal can be parsed at most two times so it is still O(n)
                node.subType = 7;
                node.add(Exp());
                pushToken(node, now, singleType(';'), new ErrInfo(ErrType.i, node.lstLine));
            }
        } else {
            node.subType = 7;
            if (expFirst()) {
                node.add(Exp());
            }
            pushToken(node, now, singleType(';'), new ErrInfo(ErrType.i, node.lstLine));
        }
        return node;
    }

    private static AstNode ForStmt() {
        AstNode node = new AstNode(AstType.ForStmt, now.line);
        node.add(LVal());
        pushToken(node, now); // =
        node.add(Exp());
        return node;
    }

    private static AstNode Exp() {
        AstNode node = new AstNode(AstType.Exp, now.line);
        node.add(AddExp());
        return node;
    }

    private static AstNode Cond() {
        AstNode node = new AstNode(AstType.Cond, now.line);
        node.add(LOrExp());
        return node;
    }

    private static AstNode LVal() {
        AstNode node = new AstNode(AstType.LVal, now.line);
        pushToken(node, now); // Ident
        if (now.type == singleType('[')) {
            pushToken(node, now); // [
            node.add(Exp());
            pushToken(node, now, singleType(']'), new ErrInfo(ErrType.k, node.lstLine));
        }
        return node;
    }

    private static AstNode PrimaryExp() {
        AstNode node = new AstNode(AstType.PrimaryExp, now.line);
        if (now.type == singleType('(')) {
            pushToken(node, now); // (
            node.add(Exp());
            pushToken(node, now, singleType(')'), new ErrInfo(ErrType.j, node.lstLine));
        } else if (now.type == TokenType.IDENFR) {
            node.add(LVal());
        } else if (now.type == TokenType.INTCON) {
            node.add(Number());
        } else if (now.type == TokenType.CHRCON) {
            node.add(Character());
        }
        return node;
    }

    private static AstNode Number() {
        AstNode node = new AstNode(AstType.Number, now.line);
        pushToken(node, now);
        return node;
    }

    private static AstNode Character() {
        AstNode node = new AstNode(AstType.Character, now.line);
        pushToken(node, now);
        return node;
    }

    private static AstNode UnaryExp() {
        AstNode node = new AstNode(AstType.UnaryExp, now.line);
        if (now.type == singleType('+') || now.type == singleType('-')
                || now.type == singleType('!')) {
            node.add(UnaryOp());
            node.add(UnaryExp());
        } else if (now.type == TokenType.IDENFR && peek(1).type == singleType('(')) {
            pushToken(node, now); // Ident
            pushToken(node, now); // (
            if (expFirst()) { // not sure ???
                node.add(FuncRParams());
            }
            pushToken(node, now, singleType(')'), new ErrInfo(ErrType.j, node.lstLine));
        } else {
            node.add(PrimaryExp());
        }
        return node;
    }

    private static AstNode UnaryOp() {
        AstNode node = new AstNode(AstType.UnaryOp, now.line);
        pushToken(node, now);
        return node;
    }

    private static AstNode FuncRParams() {
        AstNode node = new AstNode(AstType.FuncRParams, now.line);
        node.add(Exp());
        while (now.type == singleType(',')) {
            pushToken(node, now);
            node.add(Exp());
        }
        return node;
    }

    private static AstNode MulExp() {
        ArrayList<AstNode> list = new ArrayList<>();
        list.add(UnaryExp());
        while (now.type == singleType('*') || now.type == singleType('/') || now.type == singleType('%')) {
            list.add(new AstNode(now));
            next();
            list.add(UnaryExp());
        }
        return form(list, AstType.MulExp);
    }

    private static AstNode AddExp() {
        ArrayList<AstNode> list = new ArrayList<>();
        list.add(MulExp());
        while (now.type == singleType('+') || now.type == singleType('-')) {
            list.add(new AstNode(now));
            next();
            list.add(MulExp());
        }
        return form(list, AstType.AddExp);
    }

    private static AstNode RelExp() {
        ArrayList<AstNode> list = new ArrayList<>();
        list.add(AddExp());
        while (now.type == singleType('<') || now.type == singleType('>') || now.type == TokenType.LEQ ||
                now.type == TokenType.GEQ) {
            list.add(new AstNode(now));
            next();
            list.add(AddExp());
        }
        return form(list, AstType.RelExp);
    }

    private static AstNode EqExp() {
        ArrayList<AstNode> list = new ArrayList<>();
        list.add(RelExp());
        while (now.type == TokenType.EQL || now.type == TokenType.NEQ) {
            list.add(new AstNode(now));
            next();
            list.add(RelExp());
        }
        return form(list, AstType.EqExp);
    }

    private static AstNode LAndExp() {
        ArrayList<AstNode> list = new ArrayList<>();
        list.add(EqExp());
        while (now.type == TokenType.AND) {
            list.add(new AstNode(now));
            next();
            list.add(EqExp());
        }
        return form(list, AstType.LAndExp);
    }

    private static AstNode LOrExp() {
        ArrayList<AstNode> list = new ArrayList<>();
        list.add(LAndExp());
        while (now.type == TokenType.OR) {
            list.add(new AstNode(now));
            next();
            list.add(LAndExp());
        }
        return form(list, AstType.LOrExp);
    }

    private static AstNode ConstExp() {
        AstNode node = new AstNode(AstType.ConstExp, now.line);
        node.add(AddExp());
        return node;
    }
}
