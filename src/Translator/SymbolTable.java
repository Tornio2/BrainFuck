package Translator;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private final Map<String, Integer> variables = new HashMap<>();
    private int nextMemoryCell = 0;

    public void addVariable(String name) {
        variables.put(name, nextMemoryCell++);
    }

    public int getVariablePosition(String name) {
        if (!variables.containsKey(name)) {
            throw new RuntimeException("Undefined variable: " + name);
        }
        return variables.get(name);
    }

    public boolean hasVariable(String name) {
        return variables.containsKey(name);
    }

    public int getNextMemoryCell() {
        return nextMemoryCell;
    }
}