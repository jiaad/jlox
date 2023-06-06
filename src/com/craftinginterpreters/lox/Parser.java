package com.craftinginterpreters.lox;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import static com.craftinginterpreters.lox.TokenType.*;
public class Parser {
  private static class ParseError extends RuntimeException {}
  private final List<Token> tokens;
  private int current = 0;

  Parser(List<Token> tokens){
    this.tokens= tokens;
  }

  // SYNCHRONIZE

  private void synchronize(){
    advance();

    while(!isAtEnd()){
      if (previous().type == SEMICOLON) return ;

      switch(peek().type){
        case CLASS:
        case FUN:
        case VAR:
        case FOR:
        case IF:
        case WHILE:
        case PRINT:
        case RETURN:
          return;
        default:
      }

      advance();
    }
  }

  // Expr parse(){
  //   try{
  //     return expression();
  //   }catch (ParseError error){
  //     return null;
  //   }
  // }
List<Stmt>parse(){
    List<Stmt> statements = new ArrayList<>();
    while(!isAtEnd()){
      statements.add(statement());
    }
    return statements;
    
}

private Stmt statement(){
  if(match(PRINT)) return printStatement();
  return expressionStatement();
}


private Stmt printStatement(){
  Expr value = expression();
  consume(SEMICOLON, "Expect ';' after value");
  return new Stmt.Print(value);
}


private Stmt expressionStatement(){
  Expr expr = expression();
  consume(SEMICOLON, "Expect ';' after expression");
  return new Stmt.Expression(expr);
}


  // expression => equality
  // equality => comparison (sign comparison)*
  // comparison => term (sign term)*
  // factor => unary (sign unary)*
  // unary => sign unary | primary
  // primary => NUMER | STRING | true | false | nil 
    //| "(" expression ")"
  Expr expression(){
    return equality();
  }

  Expr equality(){
    Expr expr = comparison();
    while (match(BANG_EQUAL, EQUAL_EQUAL)) {
    Token operator = previous(); // because it is incremented in match
    Expr right = comparison();
    expr = new Expr.Binary(expr, operator, right);
    }
    return expr;
  }

  Expr comparison(){
    Expr expr = term();
    while(match(GREATER, LESS, GREATER_EQUAL, LESS_EQUAL)){
      Token operator = previous(); // because it is incremented in match
      Expr right = term();
      expr = new Expr.Binary(expr, operator, right);
    }
    return expr;
  }

  Expr term(){
    Expr expr = factor();
    while(match(PLUS, MINUS)){
      Token operator = previous(); // because it is incremented in match;
      Expr right = factor();
      expr = new Expr.Binary(expr, operator, right);
    }
    return expr;
  }

  Expr factor(){
    Expr expr = unary();

    while(match(SLASH, STAR)){
      Token operator = previous(); // because it is incremented in match
      Expr right = unary();
      expr = new Expr.Binary(expr, operator, right);
    }
    return expr;
  }

  Expr unary(){
    if(match(BANG, MINUS)){
      Token operator = previous();
      Expr right = unary();
      return new Expr.Unary(operator, right);
    }else {
      return primary();
    }
  }

  Expr primary(){
    if(match(NIL)) return new Expr.Literal(NIL);
    if(match(FALSE)) return new Expr.Literal(FALSE);
    if(match(TRUE)) return new Expr.Literal(TRUE);

    if(match(NUMBER, STRING)){
      return new Expr.Literal(previous().Literal);
    }

    if(match(LEFT_PAREN)){
      Expr expr = expression();
      consume(RIGHT_PAREN, "Expected ')' after expression");
      return new Expr.Grouping(expr);
    }
    
    throw error(peek(), "Expected expression");
  }

  private Token consume(TokenType type, String message){
    if(check(type)) return advance();
    throw error(peek(), message);
  }

  private boolean match(TokenType ...types){
    boolean exist = Arrays.asList(types).contains(peek().type);
    if(exist) advance();
    return exist == true;
  }

  private Token peek(){
    return tokens.get(current);
  }

  private boolean check(TokenType type){
    return peek().type == type;
  }

  // current token after increùenting the current
  private Token previous(){
    return tokens.get(current - 1);
  }

  private Token advance(){
    if(!isAtEnd()) current++;
    return previous();
  }

  private boolean isAtEnd(){
    return peek().type == EOF;
  } 

  private ParseError error(Token token, String message){
    Lox.error(token, message);
    return new ParseError();
  }
}
