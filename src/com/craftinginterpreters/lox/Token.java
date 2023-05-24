package com.craftinginterpreters.lox;

public class Token {
  final TokenType type;
  final String lexeme;
  final Object Literal;
  final int line;

  Token(TokenType type, String lexeme, Object Literal, int line ){
    this.type = type;
    this.lexeme =  lexeme; // everything like string with quotes "jiad"
    this.Literal = Literal; // deleting quotes
    this.line = line;
  }

  public String toString(){
    return this.type + " " + this.lexeme + " " + this.Literal + " " + this.line;
  }
}
