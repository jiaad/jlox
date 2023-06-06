package com.craftinginterpreters.lox;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import static com.craftinginterpreters.lox.TokenType.*;
import org.junit.Assert;


public class ScannerTest {
  @Test
  public void testScanTokens() {
    String source = "var jiad = 10; 45 67 90;";
    Scanner scanner = new Scanner(source);
    List<Token> tokens = scanner.scanTokens();

    // List<Token> expectedTokens = new ArrayList<Token>(null);
    List<Token> expectedTokens = Arrays.asList(
      new Token(VAR, "var", null, 1),
      new Token(IDENTIFIER, "jiad", null, 1),
      new Token(EQUAL, "=", null, 1),
      new Token(NUMBER, "10", 10.0, 1),
      new Token(SEMICOLON, ";", null, 1),
      new Token(NUMBER, "45", 45.0, 1),
      new Token(NUMBER, "67", 67.0, 1),
      new Token(NUMBER, "90", 90.0, 1),
      new Token(SEMICOLON, ";", null, 1),
      new Token(EOF, "", null, 1)
      );


      Assert.assertEquals(expectedTokens.size(), tokens.size());
      for(int i = 0; i < tokens.size(); i++) {
        Assert.assertEquals(expectedTokens.get(i).Literal, tokens.get(i).Literal);
        Assert.assertEquals(expectedTokens.get(i).lexeme, tokens.get(i).lexeme);
        Assert.assertEquals(expectedTokens.get(i).line, tokens.get(i).line);
        Assert.assertEquals(expectedTokens.get(i).type, tokens.get(i).type);
      }
  }
  @Test
  public void testScanTokensComment() {
    String source = "var jiad = 10; /* */";
    Scanner scanner = new Scanner(source);
    List<Token> tokens = scanner.scanTokens();

    List<Token> expectedTokens = Arrays.asList(
      new Token(VAR, "var", null, 1),
      new Token(IDENTIFIER, "jiad", null, 1),
      new Token(EQUAL, "=", null, 1),
      new Token(NUMBER, "10", 10.0, 1),
      new Token(SEMICOLON, ";", null, 1),
      new Token(EOF, "", null, 1)
      );


      Assert.assertEquals(expectedTokens.size(), tokens.size());
      for(int i = 0; i < tokens.size(); i++) {
        Assert.assertEquals(expectedTokens.get(i).Literal, tokens.get(i).Literal);
        Assert.assertEquals(expectedTokens.get(i).lexeme, tokens.get(i).lexeme);
        Assert.assertEquals(expectedTokens.get(i).line, tokens.get(i).line);
        Assert.assertEquals(expectedTokens.get(i).type, tokens.get(i).type);
      }
  }

  @Test
  public void testScanTokensNestedComment() {
    String source = "var jiad = 10; /* ** /* *** */ */  //";
    Scanner scanner = new Scanner(source);
    List<Token> tokens = scanner.scanTokens();

    List<Token> expectedTokens = Arrays.asList(
      new Token(VAR, "var", null, 1),
      new Token(IDENTIFIER, "jiad", null, 1),
      new Token(EQUAL, "=", null, 1),
      new Token(NUMBER, "10", 10.0, 1),
      new Token(SEMICOLON, ";", null, 1),
      new Token(EOF, "", null, 1)
      );


      Assert.assertEquals(expectedTokens.size(), tokens.size());
      for(int i = 0; i < tokens.size(); i++) {
        Assert.assertEquals(expectedTokens.get(i).Literal, tokens.get(i).Literal);
        Assert.assertEquals(expectedTokens.get(i).lexeme, tokens.get(i).lexeme);
        Assert.assertEquals(expectedTokens.get(i).line, tokens.get(i).line);
        Assert.assertEquals(expectedTokens.get(i).type, tokens.get(i).type);
      }
  }
}
