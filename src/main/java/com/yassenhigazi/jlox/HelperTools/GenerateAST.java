package com.yassenhigazi.jlox.HelperTools;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class GenerateAST {

    public static void main(String[] args) throws IOException {

        String outputDir = "./src/main/java/com/yassenhigazi/jlox/Parser";

        defineAst(outputDir, "ASTExpression", Arrays.asList(
                "Binary   : ASTExpression left, Token operator, ASTExpression right",
                "Call     : ASTExpression callee, Token paren, List<ASTExpression> arguments",
                "Grouping : ASTExpression expression",
                "Literal  : Object value",
                "Unary    : Token operator, ASTExpression right",
                "Variable : Token name",
                "Assign   : Token name, ASTExpression value",
                "Logical  : ASTExpression left, Token operator, ASTExpression right"
        ));

        defineAst(outputDir, "ASTStatement", Arrays.asList(
                "Block      : List<ASTStatement> statements",
                "Expression : ASTExpression expression",
                "Function   : Token name, List<Token> params, List<ASTStatement> body",
                "Print      : ASTExpression expression",
                "Return     : Token keyword, ASTExpression value",
                "Var        : Token name, ASTExpression initializer",
                "While      : ASTExpression condition, ASTStatement body",
                "If         : ASTExpression condition, ASTStatement thenBranch, ASTStatement elseBranch"

        ));
    }

    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, StandardCharsets.UTF_8);

        writer.println("package com.yassenhigazi.jlox.Parser;");
        writer.println();
        writer.println("import com.yassenhigazi.jlox.Scanner.Token;");
        writer.println("import java.util.List;");
        writer.println();
        writer.println("public abstract class " + baseName + " {");

        defineVisitor(writer, baseName, types);

        // The AST classes.
        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }

        writer.println();
        writer.println("  public abstract <R> R accept(Visitor<R> visitor);");

        writer.println("}");
        writer.close();
    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println("  public interface Visitor<R> {");

        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println("    R visit" + typeName + baseName + "(" + typeName + " " + "expr" + ");");
        }

        writer.println("  }");
    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
        writer.println("  public static class " + className + " extends " + baseName + " {");

        // Constructor.
        writer.println("    " + "public " + className + "(" + fieldList + ") {");

        // Store parameters in fields.
        String[] fields = fieldList.split(", ");

        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println("      this." + name + " = " + name + ";");
        }

        writer.println("    }");

        // Visitor pattern.
        writer.println();
        writer.println("    @Override");
        writer.println("    public <R> R accept(Visitor<R> visitor) {");
        writer.println("      return visitor.visit" + className + baseName + "(this);");
        writer.println("    }");

        // Fields.
        writer.println();

        for (String field : fields) {
            writer.println("    public final " + field + ";");
        }

        writer.println("  }");
    }

}
