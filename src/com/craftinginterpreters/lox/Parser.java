package com.craftinginterpreters.lox;

import java.util.Arrays;
import java.util.List;

import com.craftinginterpreters.lox.Stmt.Break;

import java.util.ArrayList;

import static com.craftinginterpreters.lox.TokenType.*;
public class Parser {
  private static class ParseError extends RuntimeException {}
  private final List<Token> tokens;
  private int loopdepth = 0;
  private int current = 0;
  private Environment environment = new Environment();

  Parser(List<Token> tokens){
    loopdepth = 0;
    this.tokens = tokens;
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
      statements.add(declaration());
    }
    return statements;
    
}

private Stmt declaration(){
  try {
    if(match(VAR)) return varDeclaration();
    if(match(FUN)) return function("function");

    return statement();
  } catch (Exception e) {
    // TODO: handle exception
    synchronize();
    return null;
  }
}

private Stmt varDeclaration(){
  Token name = consume(IDENTIFIER, "Expect variable name"); // it advances + give the previous
  Expr initializer = null;
  if(match(EQUAL)){
    initializer = expression();
  }
  consume(SEMICOLON, "Expected semicolon after variable declaration");
  return new Stmt.Var(name, initializer);
}

private Stmt.Function function(String kind){
  Token name = consume(IDENTIFIER, "Expect "+  kind + " name.");
  consume(IDENTIFIER, "Expect '(' after " + kind + " name");
  List<Token> parameters = new ArrayList<>();
  if(!check(TokenType.RIGHT_PAREN)){
    do {
      if(parameters.size() >= 255){
        error(peek(), "Can't have more than 255 parameters.");
      }

      parameters.add(consume(IDENTIFIER, "Expect parameter name."));
    } while (match(COMMA));
  }
  consume(RIGHT_PAREN, "Expect ')' after parameters.");
  consume(RIGHT_PAREN, "Expect '{' after before " + kind + " body");
  // Function(Token name, List<Token> params, List<Stmt> body)


  List<Stmt> body = block();
  return new Stmt.Function(name, parameters, body);
}


private Stmt statement(){
  if(match(PRINT)) return printStatement();
  if(match(LEFT_BRACE)){
    return new Stmt.Block(block());
  }
  if(match(IF)){
    return ifStatement();
  }
  if(match(WHILE)){
    return whileStatement();
  }

  if(match(FOR)){
    return forStatement();
  }

  if(match(BREAK)){
    return breakStatement();
  }

  // if(match(FUN)){
  //   return funStatement();
  // }

  return expressionStatement();
}

private List<Stmt> block(){
  List<Stmt> statements = new ArrayList<>();

  while(!check(RIGHT_BRACE) && !isAtEnd()){
    statements.add(declaration());
  }

  consume(RIGHT_BRACE, "Expected '}' at the end of the block"); // advances
  return statements;
}

private Stmt ifStatement(){
  // stmt.condition
  // stmt.than
  // else
  consume(LEFT_PAREN, "Expect '(' after 'if'.");
  Expr condition = expression();
  consume(RIGHT_PAREN, "Expect ')' after if condition.");
  Stmt thanBranch = statement();
  Stmt elseBranch = null;
  if(match(ELSE)){
    elseBranch = statement();
  }
  return new Stmt.If(condition, thanBranch, elseBranch);
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

private Stmt whileStatement(){
  consume(LEFT_PAREN, "Expected '(' after 'while'");
  Expr condition = expression();
  consume(RIGHT_PAREN, "Expected ')' after condition");

  try {
    loopdepth++;
    Stmt body = statement();
    return new Stmt.While(condition, body);
  } finally {
    loopdepth--;
  }
}

private Stmt forStatement(){
  // for (int i = 0; i < array.length; i++) 
  // for (;;) 
  consume(LEFT_PAREN, "Expected '(' after 'for'");
  Stmt initializer;
  if(match(SEMICOLON)){
    initializer = null;
  } else if(match(VAR)){
    initializer = varDeclaration();
  } else {
    initializer = expressionStatement();
  }

  Expr condition = null;
  if(!check(SEMICOLON)){
    condition = expression();
  }
  consume(SEMICOLON, "Expect ';' after loop condition.");
  Expr increment = null;
  if(!check(RIGHT_PAREN))
    increment = expression();

  consume(RIGHT_PAREN, "Expected ')' after 'condition");
  try {
    loopdepth++;

    Stmt body = statement();

    // make it while loop in the core
    if(increment != null){
      body = new Stmt.Block(
        Arrays.asList(body, new Stmt.Expression(increment))
      );
    }

    if(condition == null){
      condition = new Expr.Literal(true);
    }
    body = new Stmt.While(condition, body);

    if(initializer != null){
      body = new Stmt.Block(Arrays.asList(initializer, body));
    }
    return body;

  } finally{
      this.loopdepth--;
  }
}

private Stmt breakStatement(){
  consume(SEMICOLON, "Expected ';' after break statement");
  return new Stmt.Break();
}

  // expression => equality
  // equality => comparison (sign comparison)*
  // comparison => term (sign term)*
  // factor => unary (sign unary)*
  // unary => sign unary | primary
  // primary => NUMER | STRING | true | false | nil 
    //| "(" expression ")"
  //! MOST IMPORTANT FUNCTION TO GET THE EXPRESSIONS
  Expr expression(){
    return assignement();
  }



  Expr assignement(){
    // Expr expr = equality();
    Expr expr = or();

    if(match(EQUAL)){
      Token equals =  previous();
      // ASSIGNMENT IS RIGHT ASSICIATIVE 
      // WE DONT LOOP BUT BUT WE RECURSIVELY CALL ASSIGNEMNT() TO PARSE RIGHT HAND SIDE
      Expr value = assignement();

      if(expr instanceof Expr.Variable){
        Token name = ((Expr.Variable)expr).name;
        return new Expr.Assign(name, value);
      }
      error(equals, "Invalid assignment Target.");
    }
    return expr;
  }

  Expr or(){
    Expr expr = and();
    while(match(OR)){
      Token operator = previous();
      Expr right = and();
      expr = new Expr.Logical(expr, operator, right);
    }
    return expr;
  }

  Expr and(){
    Expr expr = equality();
    while(match(AND)){
      Token opertor = previous();
      Expr right = equality();
      expr = new Expr.Logical(expr, opertor, right);
    }
    return expr;
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
      return call();
    }
  }

  Expr call(){
    // call = primary ( ")" argumets* ")")*
    Expr expr = primary();
    while(true){
      if(match(LEFT_PAREN)){
        expr = finishCall(expr);
      } else break;
    }

    return expr;
  }

  private Expr finishCall(Expr callee){
  List<Expr> args = new ArrayList<Expr>();
  // Argumts = expression ( ','expression')*
  if(!check(RIGHT_PAREN)){
    do{
      if(args.size() >= 255){
        error(peek(), "can't have more than 255 argumants");
      }
      args.add(expression());
    }while(match(COMMA));
  }
  Token paren = consume(RIGHT_PAREN, "Expexted ')' after arguments"); /// it advanceds the token
  return new Expr.Call(callee, paren, args);
  }

  /**
   * PRIMARY 
   * true | false | nil
   * NUMBER | STRING
   * "(" expression ")"
   * IDENTIFIER
   * @return
   */
  Expr primary(){
    if(match(NIL)) return new Expr.Literal(null);
    if(match(FALSE)) return new Expr.Literal(false);
    if(match(TRUE)) return new Expr.Literal(true);

    if(match(NUMBER, STRING)){
      return new Expr.Literal(previous().Literal);
    }

    if(match(LEFT_PAREN)){
      Expr expr = expression();
      consume(RIGHT_PAREN, "Expected ')' after expression");
      return new Expr.Grouping(expr);
    }

    
    if(match(IDENTIFIER)){
      return new Expr.Variable(previous());
    }


    throw error(peek(), " - Expected expression - Implement");
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
