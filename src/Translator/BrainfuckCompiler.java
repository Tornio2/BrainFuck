package Translator;

import java.io.BufferedWriter;
import java.io.FileWriter;
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

            // Write the Brainfuck code to a file
            BufferedWriter writer = new BufferedWriter(new FileWriter("output.bf"));
            writer.write(brainfuckCode);
            writer.close();

            System.out.println("Compilation successful. Brainfuck code written to output.bf");
            System.out.println("Generated Brainfuck code:");
            System.out.println(brainfuckCode);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
        }
    }
}