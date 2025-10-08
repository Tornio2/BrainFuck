package Translator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class BrainfuckCompiler {
    public static void main(String[] args) {
        try {
            String sourceCode = new String(Files.readAllBytes(Paths.get("src/Translator/program.txt")));


            Lexer lexer = new Lexer(sourceCode);
            Parser parser = new Parser(lexer);

            String brainfuckCode = parser.parse();
            System.out.println("Generated Brainfuck Code:");
            System.out.println(brainfuckCode);

            // Optionally save to a file
            if (args.length > 1) {
                Files.write(Paths.get(args[1]), brainfuckCode.getBytes());
                System.out.println("Output saved to " + args[1]);
            }

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("Compilation error: " + e.getMessage());
        }
    }
}