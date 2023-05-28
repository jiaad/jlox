


SRC_DIR := src
OUT_DIR := bin
MAIN := 		com.craftinginterpreters.lox.Lox;
TOOL_DIR := com.craftinginterpreters.tool.GenerateAst

.PHONY: all clean compile run

all: clean compile run

clean: 
	rm -f $$(find $(OUT_DIR) -name .*class)

compile: 
	mkdir -p $(OUT_DIR)
	javac -d $(OUT_DIR) $$(find $(SRC_DIR) -name *.java)

run: 
	java -XX:+ShowCodeDetailsInExceptionMessages -cp $(OUT_DIR) $(MAIN)

gen-ast:
	java -XX:+ShowCodeDetailsInExceptionMessages -cp $(OUT_DIR) $(TOOL_DIR) src/com/craftinginterpreters/lox

#java -XX:+ShowCodeDetailsInExceptionMessages -cp build/classes com.craftinginterpreters.tool.GenerateAst src/com/craftinginterpreters/lox
# java -XX:+ShowCodeDetailsInExceptionMessages -cp $(OUT_DIR) com.craftinginterpreters.tool.GenerateAst src/com/craftinginterpreters/lox 