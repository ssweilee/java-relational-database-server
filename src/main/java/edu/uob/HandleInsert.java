package edu.uob;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class HandleInsert {

    private final FileManage fileManage;

    public HandleInsert(FileManage fileManage) {
        this.fileManage = fileManage;
    }

    public String handleInsert(ArrayList<String> tokens) throws IOException {

        String intoString = tokens.get(1).toLowerCase();
        String valuesString = tokens.get(3).toLowerCase();

        if (!intoString.toUpperCase().equals("INTO") || !valuesString.toUpperCase().equals("VALUES")) {
            return "[ERROR] Invalid INSERT syntax";
        }

        String tableName = tokens.get(2).toLowerCase();
        File fileToOpen = fileManage.getTableFile(tableName);
        if (!fileManage.tableExists(fileToOpen)) {
            return "[ERROR] Table does not exist";
        }

        List<String> valueTokens = tokens.subList(4, tokens.size());
        if (!valueTokens.get(0).equals("(") || !valueTokens.get(valueTokens.size() - 1).equals(")")) {
            return "[ERROR] Invalid value syntax: missing parentheses";
        }

        List<String> valueList = new ArrayList<>();
        for (int i = 1; i < valueTokens.size() - 1; i++) {
            String token = valueTokens.get(i);
            if (!token.equals(",")) {
                valueList.add(token);
            }
        }
        if (valueList.isEmpty()) {
            return "[ERROR] No values provided";
        }

        for (String value : valueList) {
            if (isStringLiteral(value) && (!value.startsWith("'") || !value.endsWith("'"))) {
                return "[ERROR] String must be enclosed in single quotes: " + value;
            }
        }

        List<String> cleanedValues = new ArrayList<>();
        for (String value : valueList) {
            String conditionValue = value.trim();
            if (conditionValue.equalsIgnoreCase("TRUE") || conditionValue.equalsIgnoreCase("FALSE") || conditionValue.equalsIgnoreCase("NULL")) {
                cleanedValues.add(conditionValue.toUpperCase());
            } else if (conditionValue.startsWith("'") && conditionValue.endsWith("'")) {
                cleanedValues.add(conditionValue.substring(1, conditionValue.length() - 1));
            } else {
                cleanedValues.add(conditionValue);
            }
        }

        List<String> lines;
        try {
            lines = fileManage.readTableFile(fileToOpen);
        } catch (IOException e) {
            return "[ERROR] Failed to read table file: " + (e.getMessage() != null ? e.getMessage() : "Unknown error");
        }

        int maxId = 0;
        for (int i = 1; i < lines.size(); i++) {
            String[] fields = lines.get(i).split("\t");
            int id = Integer.parseInt(fields[0]);
            if (id > maxId) maxId = id;
        }
        String newId = String.valueOf(maxId + 1);
        String newRow = newId + "\t" + String.join("\t", cleanedValues);

        lines.add(newRow);
        try {
            fileManage.writeTableFile(fileToOpen, lines);
        } catch (IOException e) {
            return "[ERROR] Failed to write table file: " + (e.getMessage() != null ? e.getMessage() : "Unknown error");
        }

        return "[OK]";
    }

    private boolean isStringLiteral(String value) {
        if (value.equalsIgnoreCase("TRUE") || value.equalsIgnoreCase("FALSE") || value.equalsIgnoreCase("NULL")) {
            return false;
        }
        if (Pattern.matches("-?\\d+\\.\\d+", value) || Pattern.matches("-?\\d+", value)) {
            return false;
        }
        return true;
    }

}
