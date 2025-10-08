package Translator;


public class BrainfuckCompiler {
    public static void main(String[] args) {
        String sourceCode = "VAR x = 5\n" +
                "VAR y = 3\n" +
                "VAR sum = x + y\n" +
                "PRINT y\n"
//                +
//                "WHILE sum > 0\n" +
//                "  PRINT sum\n" +
//                "  sum = sum - 1\n" +
//                "END"
                ;

        Lexer lexer = new Lexer(sourceCode);
        Parser parser = new Parser(lexer);
        CodeGenerator generator = new CodeGenerator();

        AST ast = parser.parse();
        String brainfuckCode = generator.generate(ast);

        System.out.println("Generated Brainfuck code:");
        System.out.println(brainfuckCode);
    }
}