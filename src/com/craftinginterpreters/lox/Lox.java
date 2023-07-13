package com.craftinginterpreters.lox;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
// import java.util.Scanner;

public class Lox {
  private static final Interpreter interpreter = new Interpreter();
  static boolean hadError = false;
  static boolean hadRunTimeError = false;
  public static void main(String[] args) throws IOException {
    if(args.length > 1){
      System.out.println("Usage: jlox [Script]");
      System.exit(64);
    } else if (args.length == 1){
      runFile(args[0]);
    }else {
      // runFile("./langtest/func.jlox");
      runPrompt();
    }
  }
  private static void runFile(String path) throws IOException {
    byte[] bytes = Files.readAllBytes(Paths.get(path));
    run(new String(bytes, Charset.defaultCharset()));
    if(hadError) System.exit(65);
    if(hadRunTimeError) System.exit(70);
  }

  private static void runPrompt() throws IOException {
    InputStreamReader input = new InputStreamReader(System.in);
    BufferedReader reader = new BufferedReader(input);

    for(;;){
      System.out.print(">");
      String line = reader.readLine();
      if(line == null) break;
      run(line);
      hadError = false;
    }
  }
  private static void run(String source){
    Scanner scanner = new Scanner(source);
    List<Token> tokens = scanner.scanTokens();
    Parser parser = new Parser(tokens);
    List<Stmt> statements = parser.parse();
    if(hadError) return;
    System.out.println(statements);
    // System.out.println(new AstPrinter().print(expression));
    Resolver resolver = new Resolver(interpreter);
    resolver.resolve(statements);
    
    
    if(hadError) return;
    //interpreter
    interpreter.interpreter(statements);
    // for (Token token : tokens) {
    //   System.out.println(token);
    // }
  }

  static void error(Token token, String message){
    if(token.type == TokenType.EOF){
      report(token.line, "", message);
    } else {
      report(token.line, "at '" + token.lexeme + "'", message);
    }
  }
  static void error(int line, String message){
      report(line, "", message);
  }

  private static void report(int line, String where, String message){
    System.err.println("[line " + line + "] Error " + where + " : " + message);
    hadError = true;
  }

  static void runtimeError(RunTimeError error){
    System.out.print(error.getMessage() + "\n[line " + error.token.line + "]");
    hadRunTimeError = true;
  }
}
