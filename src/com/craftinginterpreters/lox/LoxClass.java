package com.craftinginterpreters.lox;

import java.util.List;
import java.util.Map;

public class LoxClass implements LoxCallable {
  final String name;
  private Map<String, LoxFunction> methods;

  LoxClass(String name, Map<String, LoxFunction> methods){
    this.name = name;
    this.methods = methods;
  }

  @Override
  public int arity(){
    return 0;
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments){
    LoxInstance instance = new LoxInstance(this);
    return instance;
  }
  

  @Override
  public String toString(){
    return name;
  }

  LoxFunction findMethod(String name){
    if(methods.containsKey(name))
      return methods.get(name);
      
    return null;
  }
}
