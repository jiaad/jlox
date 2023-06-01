package com.craftinginterpreters.lox;

import java.util.List;


import static com.craftinginterpreters.lox.TokenType.*;
public class Parser {
  private final List<Token> tokens;
  private int current = 0;

  Parser(List<Token> tokens){
    this.tokens= tokens;
  }

  private Expr expression() {
    return equality();
  }

  private Expr equality(){
    Expr expr = comparison();
    while(match(BANG_EQUAL, EQUAL_EQUAL)){
      Token operator = previous();
      Expr right = comparison();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }


  // we must find !== ==
  private boolean match(TokenType... types){
    for(TokenType type : types){
      if(check(type)){
        advance();
        return true;
      }
    }

    return false;
  }
  // it checks if current type token is of given type
  private boolean check(TokenType type){
    if(isAtEnd()) return false;
    return peek().type == type;
  }

  // consume the current token and return it
  private Token advance(){
    if(!isAtEnd()) current++;
    return previous();
  }

  // verify if it is the end of file
  private boolean isAtEnd(){
    return peek().type == EOF;
  }

  // next token
  private Token peek(){
    return tokens.get(current);
  }

  // current token after increùenting the current
  private Token previous(){
    return tokens.get(current - 1);
  }
}
