
package com.craftinginterpreters.lox;

enum TokenType {
  // signle character tokens
  LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
  COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,

  // one or more character token
  BANG, BANG_EQUAL,
  EQUAL, EQUAL_EQUAL,
  GREATER, GREATER_EQUAL,
  LESS, LESS_EQUAL,

  // LITERALS
  IDENTIFIER, STRING, NUMBER,

  // KEYWORDS
  AND, CLASS, ELSE, FUN, FALSE, TRUE, FOR, IF, NIL, OR,
  PRINT, RETURN, SUPER, THIS, VAR, WHILE,

  // END

  EOF
}