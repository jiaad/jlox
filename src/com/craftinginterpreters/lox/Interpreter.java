package com.craftinginterpreters.lox;

import java.util.List;

// import com.craftinginterpreters.lox.Expr.Call;
// import com.craftinginterpreters.lox.Stmt.Break;
// import com.craftinginterpreters.lox.Stmt.Return;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
  private static class BreakException extends RuntimeException {}

  Interpreter(){
    globals.define("clock", new LoxCallable(){
      @Override
      public int arity() {
        return 0;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments){
        return (double)System.currentTimeMillis() / 1000.0;
      }

      @Override
      public String toString(){ 
        return "<native fn>"; 
      }
    });
  }

  final Environment globals = new Environment();
  private Environment environment = globals;
  private final Map<Expr, Integer> locals = new HashMap<Expr, Integer>();

  void interpreter(List<Stmt> statements){
    try {
      for (Stmt statement : statements) {
        execute(statement);
      }
    } catch (RunTimeError error) {
      Lox.runtimeError(error);
    }
  }

  private void execute(Stmt stmt){
    stmt.accept(this);
  }

  public void resolve(Expr expr, int depth){
    // only local variables
    // globals don't end up in the map
    locals.put(expr, depth);
  }

  private String stringify(Object object){
    if(object == null) return "nil";

    if(object instanceof Double){
      String text = object.toString();
      if(text.endsWith(".0")){
        text = text.substring(0, text.length() - 2);
      }
      return text;
    }

    return object.toString();
  }

  private Object evaluate(Expr expr){
    return expr.accept(this);
  }

  @Override
  public Void visitVarStmt(Stmt.Var stmt){
    Object value = null;
    if(stmt.initializer != null){
      value = evaluate(stmt.initializer);
    }
    environment.define(stmt.name.lexeme, value);
    return null;
  }

  @Override
  public Void visitExpressionStmt(Stmt.Expression stmt){
    evaluate(stmt.expression);
    return null;
  }

  // stock it in the environment
  // this is not executing but storing
  @Override
  public Void visitFunctionStmt(Stmt.Function stmt){
    // current environment
    // even inside recursive
    // it is a function and inside there is another function, it will pass the block environment
    LoxFunction function = new LoxFunction(stmt.name.lexeme,stmt.function, environment);
    environment.define(stmt.name.lexeme, function);

    return null;
  }

  @Override
  public Void visitPrintStmt(Stmt.Print stmt){
    Object value = evaluate(stmt.expression);
    System.out.println(stringify(value));
    return null;
  }


  @Override
  public Void visitBlockStmt(Stmt.Block stmt){
    executeBlock(stmt.statements, new Environment(environment));
    return null;
  }

  @Override
  public Void visitClassStmt(Stmt.Class stmt){
    environment.define(stmt.name.lexeme, null);
    LoxClass klass = new LoxClass(stmt.name.lexeme);
    environment.assign(stmt.name, klass);
    /*
     * first we define
     * then we assign
     * this two-stages var binding process helps us to refereneces to the xlass inside its own methods
     */
    return null;
  }
  @Override
  public Void visitIfStmt(Stmt.If stmt){
    // 1 -> evaluate the condition expression;
    if(isTruthy(evaluate(stmt.condition))){
      execute(stmt.thenBranch);
    }else if(stmt.elseBranch != null){
      execute(stmt.elseBranch);
    }
    return null;
  }

  @Override
  public Void visitWhileStmt(Stmt.While stmt){
    try {
      while(isTruthy(evaluate(stmt.condition))){
        execute(stmt.body);
      }
      
    } catch (BreakException e) {
      // TODO: handle exception
    }
    return null;
  }

    @Override
  public Void visitBreakStmt(Stmt.Break stmt) {
    throw new BreakException();
  }

  @Override
  public Object visitVariableExpr(Expr.Variable expr){
    // avant: return environment.get(expr.name);
    return lookupVariable(expr.name, expr);
  }

  private Object lookupVariable(Token name, Expr expr){
    // we resolve local variable
    // if it is not local || no distance it is global
    Integer distance = locals.get(expr);
    if(distance != null){
      // it means we got the result
      return environment.getAt(distance, name.lexeme);
    }else{
      return globals.get(name);
    }
  }

  @Override
  public Object visitLiteralExpr(Expr.Literal expr){
    return expr.value;
  }

  @Override
  public Object visitGroupingExpr(Expr.Grouping expr){
    return evaluate(expr.expression);
  }

  @Override
  public Object visitUnaryExpr(Expr.Unary expr){
    Object right = evaluate(expr.right);
    switch(expr.operator.type){
      case MINUS:
      checkNumberOperand(expr.operator, right);
        return -(double)right;
      case BANG:
        return !isTruthy(right);
      default:
        break;
    }
    // nreachable
    return null;
  }

  

  @Override
  public Object visitBinaryExpr(Expr.Binary expr){
    Object left = evaluate(expr.left);
    Object right = evaluate(expr.right);
    switch (expr.operator.type){
      case MINUS:
      checkNumberOperand(null, left, right);
        return (double)left - (double)right;
      case SLASH:
      checkNumberOperand(expr.operator, left, right);
      checkZeroDivision(expr.operator, (double)right);
        return (double)left / (double)right;
      case STAR:
      checkNumberOperand(expr.operator, left, right);
        return (double)left * (double)right;
      case PLUS:
        if(left instanceof Double && right instanceof Double){
          return (double)left + (double)right;
        } 
        if(left instanceof String && right instanceof String)
          return (String)left + (String)right;

        if(left instanceof String)
          return (String)left + doubleToStr((double)right);
        if(right instanceof String)
          return doubleToStr((double)left) + (String)right;

        throw new RunTimeError(expr.operator, "Operand must be two numbers or two strings");
      case GREATER:
      checkNumberOperand(expr.operator, left, right);
        return (double)left > (double)right;
      case LESS:
      checkNumberOperand(expr.operator, left, right);
      checkNumberOperand(expr.operator, left, right);
        return (double)left < (double)right;
      case GREATER_EQUAL:
      checkNumberOperand(expr.operator, left, right);
        return (double)left >= (double)right;
      case LESS_EQUAL:
      checkNumberOperand(expr.operator, left, right);
        return (double)left <= (double)right;
      case BANG_EQUAL: return !isEqual(left, right);
      case EQUAL_EQUAL: return isEqual(left, right);

      default:
      break;
    }

    /// un reachable
    return null;
  }

  @Override 
  public Object visitFunctionExpr(Expr.Function expr){
    return new LoxFunction(null, expr, environment);
  }
  @Override
  public Object visitLogicalExpr(Expr.Logical expr){
    Object left = evaluate(expr.left);
    if(expr.operator.type == TokenType.OR ){
      if(isTruthy(left)) return left;
    }else {
      //! AND
      // when isTruthy(left) is false the ! operator turns it in true, 
      // so if it is false it will send the left which is false
      // ekse 
      if(isTruthy(left) == false) return left;
    }
    return evaluate(expr.right);
  }

  //! Assignemnt not allowed to create variable
  @Override
  public Object visitAssignExpr(Expr.Assign expr){
    Object value = evaluate(expr.value);

    Integer distance = locals.get(expr);
    if(distance != null){
      environment.assignAt(distance, expr.name, value);
    } else {
      environment.assign(expr.name, value);
    }
    return value;
  }

  private boolean isTruthy(Object object){
    if(object == null) return false;
    if(object instanceof Boolean) return (boolean)object;
    return true;
  }

  private boolean isEqual(Object a, Object b){
    if(a == null && b == null) return true;
    if(a == null) return false;
    return a.equals(b);
  }

  private void checkNumberOperand(Token operator, Object operand){
    if(operand instanceof Double) return;
    throw new RunTimeError(operator, "Operand must be a number");
  }

  private void checkNumberOperand(Token operator, Object left, Object right){
    if(left instanceof Double && right instanceof Double) return;
    throw new RunTimeError(operator, "Operand must be numbers");
  }

  private String doubleToStr(double x){
    String str = String.valueOf(x);
    if(str.endsWith(".0"))
      return str.substring(0, str.length() - 2);
    return str;
  }

  private void checkZeroDivision(Token operator, Double x){
    if(x == 0)
        throw new RunTimeError(operator, "Division with zero is not permitted");
    return;
  }

  void executeBlock(List<Stmt> statements, Environment environment){
    Environment previousEnv = this.environment;
    try {
      this.environment = environment;
      for (Stmt stmt : statements) {
        // if(stmt instanceof Stmt.Break)
        //   break;
        // else
          execute(stmt);
      }
    } finally {
      this.environment = previousEnv;
    }
  }

  @Override
  public Object visitCallExpr(Expr.Call expr) {
    Object callee = evaluate(expr.callee);
    List<Object> arguments = new ArrayList<>();

    for(Expr argument: expr.arguments){
      Object arg = evaluate(argument);
      arguments.add(arg);
    }

    if(!(callee instanceof LoxCallable)){
      throw new RunTimeError(expr.paren, "Can only call functions and classes");
    }

    LoxCallable function = (LoxCallable)callee;

    if(arguments.size() != function.arity()) {
      throw new RunTimeError(expr.paren, "Expexted " + function.arity() + " arguments but got " + arguments.size());
    }
    return function.call(this, arguments);
  }

  @Override
  public Object visitGetExpr(Expr.Get expr){
    Object object = evaluate(expr.object);
    if(object instanceof LoxInstance){
      return ((LoxInstance) object).get(expr.name);
    }

    throw new RunTimeError(expr.name, "Only instances have porperties");
  }

  @Override
  public Object visitSetExpr(Expr.Set expr){
    Object object = evaluate(expr.object);

    if(!(object instanceof LoxInstance)){
      throw new RunTimeError(expr.name, "Only instances have fields");
    }

    Object value = evaluate(expr.value);
    ((LoxInstance)object).set(expr.name, value);
    return value;
  }

  @Override
  public Void visitReturnStmt(Stmt.Return stmt) {
    Object value = null;
    if(stmt.value != null) value = evaluate(stmt.value);

    throw new Return(value);
  }


  
}
