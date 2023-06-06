package com.craftinginterpreters.lox;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.craftinginterpreters.lox.TokenType.*;



public class Scanner {
  private static final Map<String, TokenType> keywords;
  private final String source;
  private final List<Token> tokens = new ArrayList<>();
  private int start = 0;
  private int current = 0;
  private int line = 1;


  static {
    keywords = new HashMap<>();
    keywords.put("and",    AND);
    keywords.put("class",  CLASS);
    keywords.put("else",   ELSE);
    keywords.put("false",  FALSE);
    keywords.put("for",    FOR);
    keywords.put("fun",    FUN);
    keywords.put("if",     IF);
    keywords.put("nil",    NIL);
    keywords.put("or",     OR);
    keywords.put("print",  PRINT);
    keywords.put("return", RETURN);
    keywords.put("super",  SUPER);
    keywords.put("this",   THIS);
    keywords.put("true",   TRUE);
    keywords.put("var",    VAR);
    keywords.put("while",  WHILE);
  }

  Scanner(String source){
    this.source = source;
  }
  private void scanToken(){
    // skipWhiteSpaces();
    char c = advance();
    switch(c){
      // skip spaces tab
      case ' ':
      case '\r':
      case '\t':
        break;
      case '\n': line++; break;
      case '"': string(); break;
      case '(': addToken(LEFT_PAREN); break;
      case ')': addToken(RIGHT_PAREN); break;
      case '{': addToken(LEFT_BRACE); break;
      case '}': addToken(RIGHT_BRACE); break;
      case ',': addToken(COMMA); break;
      case '.': addToken(DOT); break;
      case '+': addToken(PLUS); break;
      case '-': addToken(MINUS); break;
      case ';': addToken(SEMICOLON); break;
      case '*': addToken(STAR); break;
      case '!': addToken(match('=') ? BANG_EQUAL : EQUAL); break;
      case '=': addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;
      case '<': addToken(match('=') ? LESS_EQUAL : LESS); break;
      case '>': addToken(match('=') ? GREATER_EQUAL : GREATER); break;
      case '/': 
      if(match('/'))
        // A comment goes until the end of the line
        while(peek() != '\n' && !isAtEnd()) 
          advance();
      else if (match('*'))
        multilineComment();
      else
        addToken(SLASH);
      break;
      default:
      if(isAlpha(c))
        identifier();
      else if(isDigit(c))
        number();
      else
        Lox.error(line, " Uexpected character -> " + c);
      break;
    }
  }
  List<Token> scanTokens(){
    while(!isAtEnd()){
      // a chaque fois on affect le current dans le start
      // comme ca il n'est plus derriere
      start = current;
      scanToken();
    }
    tokens.add(new Token(EOF, "", null, line));
    return tokens;
  }

  private boolean isAtEnd(){
    return this.current >= source.length();
  }

  private void addToken(TokenType type){
    addToken(type, null);
  }

  private void addToken(TokenType type, Object literal){
    String text = source.substring(start, current);
    tokens.add(new Token(type, text, literal, line));
  }

  private char advance(){
    if(!isAtEnd()){
      return source.charAt(current++); // post increment
    }
    return '\0';
  }
  private boolean match(char expected){
    if(isAtEnd()) return false;
    if(source.charAt(current) != expected)
      return false;
    // it advances only if it matches
    current++;
    return true;
  }

  private char peek(){
    // this is called lockHead
    // it consumes but doesn't advance
    if(isAtEnd()) return '\0';
    return this.source.charAt(current);
  }

  private char nextPeek(){
    // this is called lockHead
    // it consumes but doesn't advance
    if(current + 1 >= source.length()) return '\0';
    return this.source.charAt(current + 1);
  }

  private void string(){
    // if not null
    while(peek() != '"' && !isAtEnd()){
      if(peek() == '\n') line++;
      advance();
    }

    if(isAtEnd()) {
      Lox.error(line, "Unterminated String");
      return;
    }

    advance(); // close the "
    String value =  source.substring(start + 1, current - 1);
    addToken(STRING, value);
  }

  private void number(){
    while(isDigit(peek()))
      advance();
    if(peek() == '.' && isDigit(nextPeek())){
      advance();
      while(isDigit(peek()))
        advance();
    }

    // advance();
    String num = source.substring(start, current);
    addToken(NUMBER, Double.parseDouble(num));
  }

  private boolean isDigit(char c){
    if(c >= 0x30 && c <= 0x39) return true;
    return false;
  }

  private boolean isAlpha(char c){
   return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
  }
  private boolean isAlphaNumeric(char c){
    return isAlpha(c) || isDigit(c);
  }

  private void identifier(){
    while(isAlphaNumeric(peek()))
      advance();
    // advance();
    String ident = source.substring(start, current);
    TokenType type = keywords.get(ident);
    if(type == null) type = IDENTIFIER;
    addToken(type);
  }

  private void multilineComment(){
    int commentCount = 1;
    // while(true && !isAtEnd()){
      while(commentCount > 0 && !isAtEnd()){
      if(peek() == '\n') line++;
      if((""+peek() + nextPeek()).equals("/*"))
        commentCount++;
      if((""+peek() + nextPeek()).equals("*/"))
        commentCount--;
      advance();
    }
    // advance();
    advance(); // advance to finish the comment */
  }

  private void skipWhiteSpaces(){
    // char[] cases = new char[]{' ', '\t', '\r'};
    while(isSpace(peek()) && !isAtEnd()){
      // System.out.println("-");
      if(peek() == '\n') line++;
      advance();
    }
    start = current;
    // advance();
  }

  private boolean isSpace(char c){
    Character[] spaces = {' ', '\n', '\t', '\r'};
    ArrayList<Character> spacess = new ArrayList<>();
    spacess.addAll(Arrays.asList(spaces));
    return spacess.contains(c);
    //.contains("oad")
  }
}
