package irGen;

import ast.AstNode;
import ast.AstType;
import ir.*;
import token.Token;
import token.TokenType;

import java.util.ArrayList;

import static utils.Utils.singleType;

public class IrGen {
    // Helper functions

    public static ValueType getValueType(TokenType type, boolean is_const, boolean is_array, boolean is_func) {
        if (is_func) {
            if (type == TokenType.VOIDTK) return ValueType.VoidFunc;
            else if (type == TokenType.INTTK) return ValueType.IntFunc;
            else if (type == TokenType.CHARTK) return ValueType.CharFunc;
        } else if (is_const) {
            if (type == TokenType.INTTK) return is_array ? ValueType.ConstIntArray : ValueType.ConstInt;
            else if (type == TokenType.CHARTK) return is_array ? ValueType.ConstCharArray : ValueType.ConstChar;
        } else {
            if (type == TokenType.INTTK) return is_array ? ValueType.IntArray : ValueType.Int;
            else if (type == TokenType.CHARTK) return is_array ? ValueType.CharArray : ValueType.Char;
        }
        return ValueType.UNKNOWN;
    }

    // Main functions

    private static Program program;
    private static Scope scope;

    public static Program generate(AstNode ast) {
        program = new Program();
        scope = Scope.create_scope(null);
        CompUnit(ast);
        return program;
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
        AstNode son = ast.get(0);
        if (son.type == AstType.ConstDecl) {
            ConstDecl(son);
        } else {
            VarDecl(son);
        }
    }

    public static void ConstDecl(AstNode ast) {
        TokenType type = BType(ast.get(1));
        for (int i = 2; i < ast.size(); i += 2) {
            ConstDef(ast.get(i), type);
        }
    }

    public static TokenType BType(AstNode ast) {
        return ast.get(0).token.type;
    }

    public static void ConstDef(AstNode ast, TokenType type) {
        Token ident = ast.get(0).token;
        boolean is_array = ast.get(1).token.type == singleType('[');
        ValueType valueType = getValueType(type, true, is_array, false);
        GlobalVariable value = new GlobalVariable(valueType, ident.value);
        if (is_array) {
            value.initializer.size = ConstExp(ast.get(2));
        } else {
            value.initializer.size = 1; // int / char  TBD
        }
        value.initializer = ConstInitVal(ast.get(-1));
        program.globalVariables.add(value);
        scope.new_value(value, ident.line);
    }

    public static Initializer ConstInitVal(AstNode ast) {
        Initializer initializer = new Initializer();
        if (ast.get(0).type == AstType.ConstExp) {
            initializer.constInt = ConstExp(ast.get(0));
        } else if (ast.get(0).token.type == TokenType.STRCON) {
            initializer.constString = ast.get(0).token.value;
        } else {
            initializer.constArrayInit = new ArrayList<>();
            for (int i = 1; i < ast.size(); i += 2) {
                initializer.constArrayInit.add(ConstExp(ast.get(i)));
            }
        }
        return initializer;
    }

    public static void VarDecl(AstNode ast) {
        TokenType type = BType(ast.get(0));
        for (int i = 1; i < ast.size(); i += 2) {
            VarDef(ast.get(i), type);
        }
    }

    public static void VarDef(AstNode ast, TokenType type) {
        Token ident = ast.get(0).token;
        boolean is_array = ast.size() > 1 && ast.get(1).token.type == singleType('[');
        ValueType valueType = getValueType(type, false, is_array, false);
        Value value = new Value(valueType, ident.value);
        if (is_array) {
            ConstExp(ast.get(2));
        }
        if (ast.get(-1).type == AstType.InitVal) {
            InitVal(ast.get(-1));
        }
        scope.new_value(value, ident.line);
    }

    public static Initializer InitVal(AstNode ast) {
        Initializer initializer = new Initializer();
        if (ast.get(0).type == AstType.Exp) {
            initializer.initValue = Exp(ast.get(0));
        } else if (ast.get(0).token.type == TokenType.STRCON) {
            initializer.constString = ast.get(0).token.value;
        } else {
            initializer.arrayInit = new ArrayList<>();
            for (int i = 1; i < ast.size(); i += 2) {
                initializer.arrayInit.add(Exp(ast.get(i)));
            }
        }
        return initializer;
    }

    public static void FuncDef(AstNode ast) {
        TokenType type = FuncType(ast.get(0));
        Token ident = ast.get(1).token;
        ValueType valueType = getValueType(type, false, false, true);
        Function function = new Function(valueType, ident.value);
        program.functions.add(function);
        scope = scope.enter();
        scope.cur_func = function;
        if (ast.get(3).type == AstType.FuncFParams) {
            FuncFParams(ast.get(3));
        }
        Block(ast.get(-1));
        scope = scope.exit();
    }

    public static void MainFuncDef(AstNode ast) {
        Function function = new Function(ValueType.IntFunc, "main");
        program.functions.add(function);
        scope = scope.enter();
        scope.cur_func = function;
        Block(ast.get(-1));
        scope = scope.exit();
    }

    public static TokenType FuncType(AstNode ast) {
        return ast.token.type;
    }

    public static void FuncFParams(AstNode ast) {
        for (int i = 0; i < ast.size(); i += 2) {
            FuncFParam(ast.get(i));
        }
    }

    public static void FuncFParam(AstNode ast) {
        TokenType type = ast.get(0).token.type;
        Token ident = ast.get(1).token;
        boolean is_array = ast.size() > 2;
        ValueType valueType = getValueType(type, false, is_array, false);
        FuncParam funcParam = new FuncParam(valueType, ident.value);
        scope.cur_func.params.add(funcParam);
        scope.new_value(funcParam, ident.line);
    }

    public static void Block(AstNode ast) {
        for (int i = 0; i < ast.size(); i++) {
            if (ast.get(i).type == AstType.BlockItem) {
                BlockItem(ast.get(i));;
            }
        }
    }

    public static void BlockItem(AstNode ast) {
        if (ast.get(0).type == AstType.Decl) {
            Decl(ast.get(0));
        } else {
            Stmt(ast.get(0));
        }
    }

    public static void Stmt(AstNode ast) {

    }

    public static void ForStmt(AstNode ast) {

    }

    public static Value Exp(AstNode ast) {
        return (Value) null;
    }

    public static void Cond(AstNode ast) {

    }

    public static void LVal(AstNode ast) {

    }

    public static void PrimaryExp(AstNode ast) {

    }

    public static void Number(AstNode ast) {

    }

    public static void Character(AstNode ast) {

    }

    public static void UnaryExp(AstNode ast) {

    }

    public static void UnaryOp(AstNode ast) {

    }

    public static void FuncRParams(AstNode ast) {

    }

    public static void MulExp(AstNode ast) {

    }

    public static void AddExp(AstNode ast) {

    }

    public static void RelExp(AstNode ast) {

    }

    public static void EqExp(AstNode ast) {

    }

    public static void LAndExp(AstNode ast) {

    }

    public static void LOrExp(AstNode ast) {

    }

    public static int ConstExp(AstNode ast) {
        return 0;
    }
}
