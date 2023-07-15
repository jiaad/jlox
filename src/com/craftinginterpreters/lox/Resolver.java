package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Stack;

/**
 * Resolver
 */
public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
  private final Interpreter interpreter;
  private final Stack<Map<String, Variable>> scopes = new Stack<>();
  private FunctionType currentFunction = FunctionType.NONE;

  private boolean inLoop = false;


  private static class Variable {
    final Token name;
    VariableState state;
    private Variable(Token name, VariableState state){
      this.name = name;
      this.state = state;
    }
  }

  private enum VariableState {
    DECLARED, DEFINED, READ
  }

  Resolver(Interpreter interpreter){
    this.interpreter = interpreter;
  }
  private enum FunctionType {
    NONE,
    FUNCTION,
    INITIALIZER,
    METHOD
  }

  void resolve(List<Stmt> statements){
    for(Stmt stmt : statements){
      resolve(stmt);
    }
  }

  void resolve(Stmt stmt){
    stmt.accept(this);
  }

  void resolve(Expr expr){
    expr.accept(this);
  }

  private void beginScope(){
    scopes.push(new HashMap<String, Variable>());
  }
  private void endScope(){
    Map<String, Variable> scope = scopes.pop();

    for (Map.Entry<String, Variable> entry : scope.entrySet()) {
      if(entry.getValue().state == VariableState.DEFINED){
          Lox.error(entry.getValue().name, "Local variable is not used.");
      }
    }
  }
  private void resolveParameters(List<Token> tokens){
    for(Token token: tokens){
      declare(token);
      define(token);
    }
  }

  private void resolveFunction(Stmt.Function stmt, FunctionType type){
    FunctionType enclosingFunction = currentFunction;
    this.currentFunction = type;
    beginScope();
      // for(Token token : stmt.function.params){
      //   declare(token);
      //   define(token);
      // }
      resolveParameters(stmt.function.params);
      resolve(stmt.function.body);
    endScope();
    this.currentFunction = enclosingFunction;
  }
  private void declare(Token name){
    //? says that it exist\
    //? it adds to innermost variable
    //? it says sets false which mean it exist but not finished resolving
    
    if(scopes.empty()) return; // if not inside a block scope
    
    Map<String, Variable> scope = scopes.peek();
    if(scope.containsKey(name.lexeme)){
      Lox.error(name, "Already a variable with this name in this scope.");
    }
    scope.put(name.lexeme, new Variable(name, VariableState.DECLARED));
  }

  private void define(Token name){
    //? after declaring the variable, we resolove its initializer expression
    //? it is done 
    if(scopes.empty()) return;
    scopes.peek().put(name.lexeme, new Variable(name, VariableState.DECLARED));
  }

  private void resolveLocal(Expr expr, Token name, boolean isRead){
    //! if we don't find anything in the scope, we assune it is global
    for(int i = scopes.size() - 1; i >= 0; i--){
      if(scopes.get(i).containsKey(name.lexeme)){
        //? scopes.size() - 1 - i
        //? suppose scopes,size == 15;
        //? we find at the current scope
        //? scopes.size - 1 to get the size of the scope which 16 - 1 = 15
        //? si 'i' is 15 too 
        //? 15 - 15 = 0
        //? if it si qt the enclosing scope it will be 1 then 2 then 3 etc...
        interpreter.resolve(expr, scopes.size() - 1 - i); //? we will push to the interperter locals

        if(isRead){
          scopes.get(i).get(name.lexeme).state = VariableState.READ;
        }
        return;
      }
    }
  }
  @Override
  public Void visitBlockStmt(Stmt.Block stmt){
    beginScope();
    resolve(stmt.statements);
    endScope();
    return null;
  }

  @Override
  public Void visitClassStmt(Stmt.Class stmt){
    declare(stmt.name);
    define(stmt.name);
  
    return null;
  }
  @Override
  public Void visitExpressionStmt(Stmt.Expression stmt){
    resolve(stmt.expression);
    return null;
  }

    @Override
  public Void visitVarStmt(Stmt.Var stmt){
    /*
    * we split binding into two steps 
    * declare then define
    *  var a = "outer";
    *  { var a = a; }
    * 
    */
    declare(stmt.name);

    if(stmt.initializer != null){
      resolve(stmt.initializer); // will return null null
    }

    define(stmt.name);
    return null;
  }
  
  @Override
  public Void visitIfStmt(Stmt.If stmt){
    resolve(stmt.condition);
    resolve(stmt.thenBranch);
    if(stmt.elseBranch != null)
      resolve(stmt.elseBranch);
    return null;
  }

  @Override
  public Void visitFunctionStmt(Stmt.Function stmt){
    declare(stmt.name);
    define(stmt.name);

    resolveFunction(stmt, FunctionType.FUNCTION);
    return null;
  }

  @Override 
  public Void visitPrintStmt(Stmt.Print stmt){
    resolve(stmt.expression);
    return null;
  }

  @Override
  public Void visitReturnStmt(Stmt.Return stmt){
    if(currentFunction == FunctionType.NONE){
      Lox.error(stmt.keyword, "can't return from top-level code.");
    }
    if(stmt.value != null)
      resolve(stmt.value);
    return null;
  }

  @Override 
  public Void visitBreakStmt(Stmt.Break stmt){
    return null;
  }

  @Override
  public Void visitWhileStmt(Stmt.While stmt){
    resolve(stmt.condition);
    resolve(stmt.body);
    return null;
  }

  @Override
  public Void visitBinaryExpr(Expr.Binary expr){
    resolve(expr.left);
    resolve(expr.right);
    return null;
  }

  @Override
  public Void visitCallExpr(Expr.Call expr){
    resolve(expr.callee);
    for (Expr argument : expr.arguments) {
      resolve(argument);
    }
    return null;
  }

  @Override
  public Void visitGetExpr(Expr.Get expr){
    resolve(expr.object);
    return null;
  }

  @Override
  public Void visitGroupingExpr(Expr.Grouping expr){
    resolve(expr.expression);
    return null;
  }

  @Override
  public Void visitLiteralExpr(Expr.Literal expr){
    //? il fait rien
    //? il n'a pas de sous expr
    return null;
  }

  @Override
  public Void visitLogicalExpr(Expr.Logical expr){
    resolve(expr.left);
    resolve(expr.right);
    return null;
  }

  @Override
  public Void visitSetExpr(Expr.Set expr){
    resolve(expr.value);
    resolve(expr.object);
    return null;
  }

  @Override
  public Void visitUnaryExpr(Expr.Unary expr){
    resolve(expr.right);
    return null;
  }


  @Override
  public Void visitAssignExpr(Expr.Assign expr){
    resolve(expr.value);
    resolveLocal(expr, expr.name, false);
    return null;
  }

  //! resolving variables
  @Override
  public Void visitVariableExpr(Expr.Variable expr){
    if(!scopes.isEmpty() && scopes.peek().containsKey(expr.name.lexeme) && scopes.peek().get(expr.name.lexeme).state == VariableState.DECLARED){
      //?? i understood, si le expr.name.lexeme exist(meme is la valeur est FALSE), c'est une erreur
      //? car expr.name.lexeme est le nom de la variable
      //? https://craftinginterpreters.com/resolving-and-binding.html#resolving-variable-expressions
      Lox.error(expr.name, "can't read local variable in it's own initializer");
    }

    resolveLocal(expr, expr.name, true);
    return null;
  }

  @Override 
  public Void visitFunctionExpr(Expr.Function expr){
    resolveParameters(expr.params);
    resolve(expr.body);
    return null;
  }
}
