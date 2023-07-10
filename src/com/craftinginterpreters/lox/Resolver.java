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
  private final Stack<Map<String, Boolean>> scopes = new Stack<>();
  private FunctionType currentFunction = FunctionType.NONE;

  private boolean inLoop = false;
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
    scopes.push(new HashMap<String, Boolean>());
  }
  private void endScope(){
    scopes.pop();
  }
  private void resolveParameters(List<Token> tokens){
    for(Token token: tokens){
      declare(token);
      define(token);
    }
  }

  private void resolveFunction(Stmt.Function stmt){
    beginScope();
      // for(Token token : stmt.function.params){
      //   declare(token);
      //   define(token);
      // }
      resolveParameters(stmt.function.params);
      resolve(stmt.function.body);
    endScope();
  }
  private void declare(Token token){
    //? says that it exist\
    //? it adds to innermost variable
    //? it says sets false which mean it exist but not finished resolving
    if(scopes.empty()) return;
    scopes.peek().put(token.lexeme, false);
  }

  private void define(Token token){
    //? after declaring the variable, we resolove its initializer expression
    //? it is done 
    if(scopes.empty()) return;
    scopes.peek().put(token.lexeme, true);
  }

  private void resolveLocal(Expr expr, Token name){
    //! if we don't find anything in the scope, we assune ie is global
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
  public Void visitFunctionStmt(Stmt.Function stmt){
    declare(stmt.name);
    define(stmt.name);

    resolveFunction(stmt);
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
  public Void visitAssignExpr(Expr.Assign expr){
    resolve(expr.value);
    resolveLocal(expr, expr.name);
    return null;
  }

  //! resolving variables
  @Override
  public Void visitVariableExpr(Expr.Variable expr){
    if(!scopes.isEmpty() && scopes.peek().get(expr.name.lexeme) == false){
      //?? i understood, si le expr.name.lexeme exist(meme is la valeur est FALSE), c'est une erreur
      //? car expr.name.lexeme est le nom de la variable
      //? https://craftinginterpreters.com/resolving-and-binding.html#resolving-variable-expressions
      Lox.error(expr.name, "can't read local variable in it's own initializer");
    }

    resolveLocal(expr, expr.name);
    return null;
  }

  
}