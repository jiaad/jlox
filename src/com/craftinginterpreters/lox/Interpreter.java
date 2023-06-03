package com.craftinginterpreters.lox;

import java.sql.Time;

import org.hamcrest.core.IsEqual;

public class Interpreter implements Expr.Visitor<Object> {
  void interpreter(Expr expression){
    try {
      Object value = evaluate(expression);
      System.out.println(stringify(value));
    } catch (RunTimeError error) {
      Lox.runtimeError(error);
    }
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
  
}
