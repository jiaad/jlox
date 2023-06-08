package com.craftinginterpreters.lox;

import java.util.List;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
  private Environment environment = new Environment();
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

  private String stringify(Object object){
    if(object == null) return "nil";

    if(object instanceof Double){
      String text= object.toString();
      if(text.endsWith(".0")){
        text = text.substring(0, text.length( ) - 2);
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

  @Override
  public Void visitPrintStmt(Stmt.Print stmt){
    Object value = evaluate(stmt.expression);
    System.out.println(stringify(value));
    return null;
  }


  @Override
  public Void visitBlockStmt(Stmt.Block statement){
    executeBlock(statement.statements, new Environment(environment));
    return null;
  }

  @Override
  public Object visitVariableExpr(Expr.Variable expr){
    return environment.get(expr.name);
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
      case EQUAL_EQUAL: return !isEqual(left, right);

      default:
      break;
    }

    /// un reachable
    return null;



  }

  //! Assignemnt not allowed to create variable
  @Override
  public Object visitAssignExpr(Expr.Assign expr){
    Object value = evaluate(expr.value);
    environment.assign(expr.name, value);
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
        execute(stmt);
      }
    } finally {
      this.environment = previousEnv;
    }
  }
  
}
