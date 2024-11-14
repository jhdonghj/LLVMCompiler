package analyse;

import ast.AstNode;
import ast.AstType;
import err.ErrHandler;
import err.ErrInfo;
import err.ErrType;
import token.Token;
import token.TokenType;
import utils.IO;

import java.util.ArrayList;
import java.util.HashMap;

import static utils.Utils.singleType;

public class Analyse {
    public enum SymType {
        ConstChar, ConstInt, ConstCharArray, ConstIntArray,
        Char, Int, CharArray, IntArray, VoidFunc, CharFunc,
        IntFunc, NOTYPE,
    }
    public static SymType getType(TokenType type, boolean is_const, boolean is_array, boolean is_func) {
        if (is_func) {
            if (type == TokenType.VOIDTK) return SymType.VoidFunc;
            else if (type == TokenType.INTTK) return SymType.IntFunc;
            else if (type == TokenType.CHARTK) return SymType.CharFunc;
        } else if (is_const) {
            if (type == TokenType.INTTK) return is_array ? SymType.ConstIntArray : SymType.ConstInt;
            else if (type == TokenType.CHARTK) return is_array ? SymType.ConstCharArray : SymType.ConstChar;
        } else {
            if (type == TokenType.INTTK) return is_array ? SymType.IntArray : SymType.Int;
            else if (type == TokenType.CHARTK) return is_array ? SymType.CharArray : SymType.Char;
        }
        return SymType.NOTYPE;
    }
    public static boolean isConst(SymType symType) {
        return symType == SymType.ConstChar || symType == SymType.ConstInt ||
                symType == SymType.ConstIntArray || symType == SymType.ConstCharArray;
    }
    public static boolean isArray(SymType symType) {
        return symType == SymType.ConstIntArray || symType == SymType.IntArray ||
                symType == SymType.ConstCharArray || symType == SymType.CharArray;
    }
    public static boolean isFunc(SymType symType) {
        return symType == SymType.VoidFunc || symType == SymType.CharFunc || symType == SymType.IntFunc;
    }
    public static SymType deArray(SymType type) {
        if (type == SymType.ConstIntArray) return SymType.ConstInt;
        if (type == SymType.IntArray) return SymType.Int;
        if (type == SymType.ConstCharArray) return SymType.ConstChar;
        if (type == SymType.CharArray) return SymType.Char;
        return type;
    }
    public static SymType deConst(SymType type) {
        if (type == SymType.ConstChar) return SymType.Char;
        if (type == SymType.ConstInt) return SymType.Int;
        if (type == SymType.ConstCharArray) return SymType.CharArray;
        if (type == SymType.ConstIntArray) return SymType.IntArray;
        return type;
    }
    public static SymType deFunc(SymType type) {
        if (type == SymType.VoidFunc) return SymType.NOTYPE;
        if (type == SymType.CharFunc) return SymType.Char;
        if (type == SymType.IntFunc) return SymType.Int;
        return type;
    }
    public static SymType base(SymType type) {
        return deConst(deArray(type));
    }
    public static SymType merge(SymType type1, SymType type2) {
        // ConstInt, ConstInt -> ConstInt; ConstInt, Int -> Int; Int, Int -> Int
        // Array, Array -> NoType; Array, Int -> Array; Int, Char -> Int
        if (type1 == SymType.NOTYPE || type2 == SymType.NOTYPE) return SymType.NOTYPE;
        if ((isArray(type1) && isArray(type2)) || isFunc(type1) || isFunc(type2)) return SymType.NOTYPE;
        boolean is_array = isArray(type1) || isArray(type2);
        boolean is_const = isConst(type1) && isConst(type2);
        TokenType type = base(type1) == SymType.Int || base(type2) == SymType.Int ? TokenType.INTTK : TokenType.CHARTK;
        return getType(type, is_const, is_array, false);
    }

    public static class Symbol {
        public int scope_id;
        public SymType type;
        public String name;
        public ArrayList<SymType> params;

        public Symbol(SymType type, String name) {
            scope_id = cur_id();
            this.type = type;
            this.name = name;
            params = new ArrayList<>();
        }
    }

    public static int id;
    public static ArrayList<HashMap<String, Symbol>> table;
    public static ArrayList<Integer> tabId;
    public static SymType cur_func;
    public static int loop_cnt;
    public static void enter() {
        id += 1;
        table.add(new HashMap<>());
        tabId.add(id);
    }
    public static void exit() {
        table.remove(table.size() - 1);
        tabId.remove(tabId.size() - 1);
    }
    public static HashMap<String, Symbol> cur_table() {
        return table.get(table.size() - 1);
    }
    public static int cur_id() {
        return tabId.get(tabId.size() - 1);
    }
    public static void putSymbol(String name, Symbol symbol, int line) {
        if (cur_table().containsKey(name)) {
            ErrHandler.addError(new ErrInfo(ErrType.b, line));
        } else {
            cur_table().put(name, symbol);
            IO.addSymbol(symbol);
        }
    }
    public static Symbol getSymbol(String name, int line) {
        for (int i = table.size() - 1; i >= 0; i--) {
            if (table.get(i).containsKey(name)) {
                return table.get(i).get(name);
            }
        }
        ErrHandler.addError(new ErrInfo(ErrType.c, line));
        return null;
    }

    //////////// main functions ////////////

    public static void analyse(AstNode ast) {
        id = 0;
        table = new ArrayList<>();
        tabId = new ArrayList<>();
        cur_func = null;
        loop_cnt = 0;
        enter();
        CompUnit(ast);
    }

    public static void CompUnit(AstNode ast) {
        for (AstNode son : ast.sons) {
            if (son.type == AstType.Decl) {
                Decl(son);
            } else if (son.type == AstType.FuncDef) {
                FuncDef(son);
            } else {
                MainFuncDef(son);
            }
        }
    }

    public static void Decl(AstNode ast) {
        if (ast.get(0).type == AstType.ConstDecl) {
            ConstDecl(ast.get(0));
        } else {
            VarDecl(ast.get(0));
        }
    }

    public static void ConstDecl(AstNode ast) {
        TokenType type = Btype(ast.get(1));
        for (AstNode son : ast.sons) {
            if (son.type == AstType.ConstDef) {
                ConstDef(son, type);
            }
        }
    }

    public static TokenType Btype(AstNode ast) {
        return ast.get(0).token.type;
    }

    public static void ConstDef(AstNode ast, TokenType type) {
        Token ident = ast.get(0).token;
        boolean is_array = ast.size() > 3;
        SymType symType = getType(type, true, is_array, false);
        Symbol symbol = new Symbol(symType, ident.value);
        for (AstNode son : ast.sons) {
            if (son.type == AstType.ConstExp) {
                ConstExp(son);
            } else if (son.type == AstType.ConstInitVal) {
                ConstInitVal(son);
            }
        }
        putSymbol(ident.value, symbol, ident.line);
    }

    public static void ConstInitVal(AstNode ast) {
        for (AstNode son : ast.sons) {
            if (son.type == AstType.ConstExp) {
                ConstExp(son);
            }
        }
    }

    public static void VarDecl(AstNode ast) {
        TokenType type = Btype(ast.get(0));
        for (AstNode son : ast.sons) {
            if (son.type == AstType.VarDef) {
                VarDef(son, type);
            }
        }
    }

    public static void VarDef(AstNode ast, TokenType type) {
        Token ident = ast.get(0).token;
        boolean is_array = ast.size() > 1 && ast.get(1).token.type == singleType('[');
        SymType symType = getType(type, false, is_array, false);
        Symbol symbol = new Symbol(symType, ident.value);
        for (AstNode son : ast.sons) {
            if (son.type == AstType.ConstExp) {
                ConstExp(son);
            } else if (son.type == AstType.InitVal) {
                InitVal(son);
            }
        }
        putSymbol(ident.value, symbol, ident.line);
    }

    public static void InitVal(AstNode ast) {
        for (AstNode son : ast.sons) {
            if (son.type == AstType.Exp) {
                Exp(son);
            }
        }
    }

    public static void FuncDef(AstNode ast) {
        TokenType type = FuncType(ast.get(0));
        Token ident = ast.get(1).token;
        SymType symType = getType(type, false, false, true);
        Symbol symbol = new Symbol(symType, ident.value);
        putSymbol(ident.value, symbol, ident.line);
        enter();
        cur_func = symType;
        boolean is_last_ret = false;
        for (AstNode son : ast.sons) {
            if (son.type == AstType.FuncFParams) {
                FuncFParams(son, symbol);
            } else if (son.type == AstType.Block) {
                is_last_ret = Block(son);
            }
        }
        cur_func = null;
        exit();
        if (!is_last_ret && symType != SymType.VoidFunc) {
            ErrHandler.addError(new ErrInfo(ErrType.g, ast.lstLine));
        }
    }

    public static void MainFuncDef(AstNode ast) {
        enter();
        cur_func = SymType.IntFunc;
        boolean is_last_ret = Block(ast.get(4));
        cur_func = null;
        exit();
        if (!is_last_ret) {
            ErrHandler.addError(new ErrInfo(ErrType.g, ast.get(4).lstLine));
        }
    }

    public static TokenType FuncType(AstNode ast) {
        return ast.get(0).token.type;
    }

    public static void FuncFParams(AstNode ast, Symbol func) {
        for (AstNode son : ast.sons) {
            if (son.type == AstType.FuncFParam) {
                FuncFParam(son, func);
            }
        }
    }

    public static void FuncFParam(AstNode ast, Symbol func) {
        TokenType type = Btype(ast.get(0));
        Token ident = ast.get(1).token;
        boolean is_array = ast.size() > 2;
        SymType symType = getType(type, false, is_array, false);
        func.params.add(symType);
        Symbol symbol = new Symbol(symType, ident.value);
        putSymbol(ident.value, symbol, ident.line);
    }

    public static boolean Block(AstNode ast) {
        boolean is_last_ret = false;
        for (int i = 1; i + 1 < ast.size(); i++) {
            is_last_ret = BlockItem(ast.get(i));
        }
        return is_last_ret;
    }

    public static boolean BlockItem(AstNode ast) {
        if (ast.get(0).type == AstType.Decl) {
            Decl(ast.get(0));
            return false;
        } else {
            return Stmt(ast.get(0));
        }
    }

    public static boolean Stmt(AstNode ast) {
        if (ast.subType == 1 || ast.subType == 2) { // if / for
            if (ast.subType == 2) {
                loop_cnt++;
            }
            for (AstNode son : ast.sons) {
                if (son.type == AstType.Cond) {
                    Cond(son);
                } else if (son.type == AstType.Stmt) {
                    Stmt(son);
                } else if (son.type == AstType.ForStmt) {
                    ForStmt(son);
                }
            }
            if (ast.subType == 2) {
                loop_cnt--;
            }
        } else if (ast.subType == 3) { // break / continue
            // check if in loop
            if (loop_cnt == 0) {
                ErrHandler.addError(new ErrInfo(ErrType.m, ast.get(0).line));
            }
        } else if (ast.subType == 4) { // block
            enter();
            Block(ast.get(0));
            exit();
        } else if (ast.subType == 5) { // printf
            // check meet
            ArrayList<SymType> params = FuncRParams(ast);
            // using FuncRParams to extract param types
            int j = 0;
            String format = ast.get(2).token.value;
            for (int i = 1; i < format.length(); i++) {
                if (format.charAt(i - 1) == '%') {
                    char ch = format.charAt(i);
                    if (ch == 'c' || ch == 'd') {
                        j++;
                    }
                }
            }
            if (j != params.size()) {
                ErrHandler.addError(new ErrInfo(ErrType.l, ast.get(0).line));
            }
        } else if (ast.subType == 6) { // return
            // check return func
            boolean has_exp = false;
            for (AstNode son : ast.sons) {
                if (son.type == AstType.Exp) {
                    Exp(son);
                    has_exp = true;
                }
            }
            if (cur_func == SymType.VoidFunc && has_exp) {
                ErrHandler.addError(new ErrInfo(ErrType.f, ast.get(0).line));
            }
            return true;
        } else if (ast.subType == 7) { // exp
            for (AstNode son : ast.sons) {
                if (son.type == AstType.Exp) {
                    Exp(son);
                }
            }
        } else if (ast.subType == 8) { // getint / getchar
            SymType type = LVal(ast.get(0));
            if (isConst(type)) {
                ErrHandler.addError(new ErrInfo(ErrType.h, ast.get(0).line));
            }
        } else if (ast.subType == 9) {
            SymType type = LVal(ast.get(0));
            if (isConst(type)) {
                ErrHandler.addError(new ErrInfo(ErrType.h, ast.get(0).line));
            }
            Exp(ast.get(2));
        }
        return false;
    }

    public static void ForStmt(AstNode ast) {
        SymType type = LVal(ast.get(0));
        if (isConst(type)) {
            ErrHandler.addError(new ErrInfo(ErrType.h, ast.get(0).line));
        }
        Exp(ast.get(2));
    }

    public static SymType Exp(AstNode ast) {
        return AddExp(ast.get(0));
    }

    public static void Cond(AstNode ast) {
        LOrExp(ast.get(0));
    }

    public static SymType LVal(AstNode ast) {
        Token ident = ast.get(0).token;
        Symbol sym = getSymbol(ident.value, ident.line);
        SymType type = sym == null ? SymType.NOTYPE : sym.type;
        if (ast.size() > 1) {
            Exp(ast.get(2));
            type = deArray(type);
        }
        return type;
    }

    public static SymType PrimaryExp(AstNode ast) {
        if (ast.size() > 1) {
            return Exp(ast.get(1));
        } else if (ast.get(0).type == AstType.LVal) {
            return LVal(ast.get(0));
        } else if (ast.get(0).type == AstType.Number) {
            return Number(ast.get(0));
        } else if (ast.get(0).type == AstType.Character) {
            return Character(ast.get(0));
        }
        return SymType.NOTYPE;
    }

    public static SymType Number(AstNode ast) {
        return SymType.ConstInt;
    }

    public static SymType Character(AstNode ast) {
        return SymType.ConstChar;
    }

    public static SymType UnaryExp(AstNode ast) {
        if (ast.size() == 1) {
            return PrimaryExp(ast.get(0));
        } else if(ast.size() == 2) {
            return merge(UnaryOp(ast.get(0)), UnaryExp(ast.get(1)));
        } else { // function call
            Token ident = ast.get(0).token;
            Symbol sym = getSymbol(ident.value, ident.line);
            if (sym == null) return SymType.NOTYPE;
            ArrayList<SymType> params = FuncRParams(ast.get(2));
            if (params.size() != sym.params.size()) {
                ErrHandler.addError(new ErrInfo(ErrType.d, ident.line));
            } else {
                for (int i = 0; i < params.size(); i++) {
                    SymType type1 = params.get(i), type2 = sym.params.get(i);
                    if (isArray(type1) != isArray(type2) ||
                            (isArray(type1) && base(type1) != base(type2))) {
                        ErrHandler.addError(new ErrInfo(ErrType.e, ident.line));
                        break;
                    }
                }
            }
            return deFunc(sym.type);
        }
    }

    public static SymType UnaryOp(AstNode ast) {
        return SymType.ConstInt;
    }

    public static ArrayList<SymType> FuncRParams(AstNode ast) {
        ArrayList<SymType> params = new ArrayList<>();
        for (AstNode son : ast.sons) {
            if (son.type == AstType.Exp) {
                params.add(Exp(son));
            }
        }
        return params;
    }

    public static SymType MulExp(AstNode ast) {
        if (ast.size() == 1) {
            return UnaryExp(ast.get(0));
        } else {
            return merge(MulExp(ast.get(0)), UnaryExp(ast.get(2)));
        }
    }

    public static SymType AddExp(AstNode ast) {
        if (ast.size() == 1) {
            return MulExp(ast.get(0));
        } else {
            return merge(AddExp(ast.get(0)), MulExp(ast.get(2)));
        }
    }

    public static void RelExp(AstNode ast) {
        if (ast.size() == 1) {
            AddExp(ast.get(0));
        } else {
            RelExp(ast.get(0));
            AddExp(ast.get(2));
        }
    }

    public static void EqExp(AstNode ast) {
        if (ast.size() == 1) {
            RelExp(ast.get(0));
        } else {
            EqExp(ast.get(0));
            RelExp(ast.get(2));
        }
    }

    public static void LAndExp(AstNode ast) {
        if (ast.size() == 1) {
            EqExp(ast.get(0));
        } else {
            LAndExp(ast.get(0));
            EqExp(ast.get(2));
        }
    }

    public static void LOrExp(AstNode ast) {
        if (ast.size() == 1) {
            LAndExp(ast.get(0));
        } else {
            LOrExp(ast.get(0));
            LAndExp(ast.get(2));
        }
    }

    public static SymType ConstExp(AstNode ast) {
        return AddExp(ast.get(0));
    }
}
