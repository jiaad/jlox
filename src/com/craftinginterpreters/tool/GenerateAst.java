package com.craftinginterpreters.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
public class GenerateAst {
  public static void main(String[] args) throws IOException {
    if(args.length != 1){
      System.err.println("Usage: generate ast <output directory>");
      System.exit(64);
    }
    String outputDir = args[0];

    defineAst(outputDir, "Expr", Arrays.asList(
      "Assign: Token name, Expr value",
      "Binary     : Expr left, Token operator, Expr right",
      "Function   : List<Token> params, List<Stmt> body",
      "Call       : Expr callee, Token paren, List<Expr> arguments",
      "Get        : Expr object, Token name",
      "Set        : Expr object, Token name, Expr value",
      "Grouping   : Expr expression",
      "Literal    : Object value",
      "Unary      : Token operator, Expr right",
      "Variable   : Token name",
      "Logical    : Expr left, Token operator, Expr right"
    ));

    defineAst(outputDir, "Stmt", Arrays.asList(
      "Block      : List<Stmt> statements",
      "Class      : Token name, List<Stmt.Function> methods",
      "Break      : ",
      "Expression : Expr expression",
      "Function   : Token name, Expr.Function function",
      "If         : Expr condition, Stmt thenBranch, Stmt elseBranch",
      "Print      : Expr expression",
      "Return     : Token keyword, Expr value",
      "Var        : Token name, Expr initializer",
      "While      : Expr condition, Stmt body"
    ));
  }

  private static void defineAst( String outputDir, String baseName, List<String> types ) throws IOException {
    String path = outputDir + "/" + baseName + ".java";
    PrintWriter writer = new PrintWriter(path, "UTF-8");

    writer.println("package com.craftinginterpreters.lox;");
    writer.println();
    writer.println("import java.util.List;");
    writer.println();
    writer.println("abstract class " + baseName + "{");

    // VISITOR
    defineVisitor(writer, baseName, types);
    // the AST classes
    for(String type : types){
      String className = type.split(":")[0].trim();
      String fields = type.split(":")[1].trim();
      defineType(writer, baseName, className, fields);
    }

    // the base acctpt method

    writer.println();
    writer.println("  abstract <R> R accept(Visitor<R> visitor);");


    writer.println("}");
    writer.close();
  }

  private static void defineType(PrintWriter writer, String baseName, String className, String fieldList){
    writer.println("  static class " + className + " extends " + baseName + " {");

    // constructor
    writer.println("    " + className + "(" + fieldList + ") {");

    // strore paramater
    String[] fields;
    if(fieldList.isEmpty()){
      fields = new String[0];
    } else {
      fields = fieldList.split(", ");
    }

    for(String field : fields){
      String name = field.split(" ")[1];
      writer.println("      this." + name + " = "+ name + ";");
    }

    writer.println("    }");

    // Visitor pattern
    writer.println();
    writer.println("    @Override");
    writer.println("    <R> R accept(Visitor<R> visitor) {");
    writer.println("     return visitor.visit" + className + baseName + "(this);");
    writer.println("    }");
    

    // Fields
    writer.println();
    for(String field: fields){
      writer.println("    final " + field + ";");
    }

    writer.println("  }");
    writer.println();
  }

  private static void defineVisitor(PrintWriter writer, String baseName, List<String> types){
    writer.println("  interface Visitor<R> {");
    for (String type : types) {
      String typeName = type.split(":")[0].trim();
      writer.println("    R visit" + typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");
      
    }

    writer.println("  }");
  }
}