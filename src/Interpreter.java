import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

/*
    * A simple interpreter for a Brainfuck-like esoteric programming language.
    * The language consists of the following commands:
    * > : Increment the data pointer (to point to the next cell to the right).
    * < : Decrement the data pointer (to point to the next cell to the left
    * + : Increment (increase by one) the byte at the data pointer.
    * - : Decrement (decrease by one) the byte at the data pointer.
    * . : Output the byte at the data pointer as a character.
    * , : Input a byte and store it in the byte at the data pointer.
    * [ : If the byte at the data pointer is zero, then instead of moving the
    * instruction pointer forward to the next command, jump it forward to the command
    * after the matching ] command.
    * ] : If the byte at the data pointer is nonzero, then instead of moving the
    * instruction pointer forward to the next command, jump it back to the command
    * after the matching [ command.
    * The interpreter uses an array of 30,000 bytes initialized to zero as memory.
    * The data pointer starts at the beginning of this array.
    * The program to be interpreted is read from a .txt file.
 */

public class Interpreter {
    private static final int MEMORY_SIZE = 30000;
    private static final int EOF = -1;

    public static void main(String[] args) {
        try {
            // specify the .txt file with the program
            String program = convertFileToProgram2("src/GameOfLife.txt");
            // execute the program
            execute(program);
        } catch (IOException e) {
            System.err.println("Error reading program file: " + e.getMessage());
        }
    }


    // converts the contents of a file to a single string - basic version
    private static String convertFileToProgram(String filename) throws IOException {
        StringBuilder program = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            int c;
            while ((c = reader.read()) != EOF) {
                program.append((char) c);
            }
        }
        return program.toString();
    }

    // converts the contents of a file to a single string, ignoring comments starting with # ( even special characters)
    private static String convertFileToProgram2(String filename) throws IOException {
        StringBuilder program = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            int c;
            boolean inComment = false;

            while ((c = reader.read()) != EOF) {
                char currentChar = (char) c;

                if (currentChar == '#') {
                    inComment = true;
                } else if (currentChar == '\n') {
                    inComment = false;
                } else if (!inComment) {
                    program.append(currentChar);
                }
            }
        }
        return program.toString();
    }


    private static void execute(String program) {
        byte[] memory = new byte[MEMORY_SIZE];
        int pointer = 0; // Data Pointer
        int PC = 0; // Program Counter

        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

        try {
            while (PC < program.length()) {
                char instruction = program.charAt(PC);

                switch (instruction) {
                    case '>':
                        // increment the data pointer; move to the next cell
                        pointer = (pointer + 1) % MEMORY_SIZE; // wrap around if overflow
                        break;
                    case '<':
                        // decrement the data pointer; move to the previous cell
                        pointer = (pointer - 1 + MEMORY_SIZE) % MEMORY_SIZE; // wrap around if overflow
                        break;
                    case '+':
                        // increment the byte at the pointer
                        memory[pointer]++;
                        break;
                    case '-':
                        // decrement the byte at the pointer
                        memory[pointer]--;
                        break;
                    case '.':
                        // output the byte at the pointer
                        System.out.print((char) (memory[pointer] & 0xFF));
                        break;
                    case ',':
                        // input a byte and store it at the pointer
                        int inputByte = input.read();
                        memory[pointer] = (byte) (inputByte == EOF ? 0 : inputByte);
                        break;
                    case '[':
                        // if the byte at the pointer is zero, jump forward to the matching ]
                        if (memory[pointer] == 0) {
                            int bracketCount = 1;
                            while (bracketCount > 0) {
                                PC++;
                                if (PC >= program.length()) {
                                    throw new RuntimeException("Unmatched [ bracket");
                                }
                                char c = program.charAt(PC);
                                if (c == '[') bracketCount++;
                                else if (c == ']') bracketCount--;
                            }
                        }
                        break;
                    case ']':
                        // if the byte at the data pointer is nonzero, jump back to the matching [
                        if (memory[pointer] != 0) {
                            int bracketCount = 1;
                            while (bracketCount > 0) {
                                PC--;
                                if (PC < 0) {
                                    throw new RuntimeException("Unmatched ] bracket");
                                }
                                char c = program.charAt(PC);
                                if (c == ']') bracketCount++;
                                else if (c == '[') bracketCount--;
                            }
                        }
                        break;
                }

                PC++;
            }
        } catch (IOException e) {
            System.err.println("Error reading input: " + e.getMessage());
        }
    }
}