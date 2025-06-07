package edu.uob;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HandleAlter {

    private final FileManage fileManage;

    public HandleAlter(FileManage fileManage) {
        this.fileManage = fileManage;
    }

    public String handleAlter(ArrayList<String> tokens) {

        try {
            String tableString = tokens.get(1).toUpperCase();
            String tableNameString = tokens.get(2).toLowerCase();
            String alterationTypeString = tokens.get(3).toUpperCase();
            String attributeNameString = tokens.get(4);

            File fileToOpen = fileManage.getTableFile(tableNameString);
            if (!fileManage.tableExists(fileToOpen)) {
                return "[ERROR] Table " + tableNameString + " does not exists";
            }

            if (!tableString.equals("TABLE") || !(alterationTypeString.equals("ADD") || alterationTypeString.equals("DROP")) || tokens.size() < 5) {
                return "[ERROR] Invalid ALTER syntax";
            }

            List<String> exsistlines = fileManage.readTableFile(fileToOpen);

            if (exsistlines.isEmpty()) {
                if (alterationTypeString.equals("ADD")) {
                    exsistlines.add(attributeNameString);
                    Files.write(fileToOpen.toPath(), exsistlines);
                    return "[OK]";
                }
                return "[ERROR] Table is empty, can't drop column";
            }

            List<String> firstRow = new ArrayList<>(Arrays.asList(exsistlines.get(0).split("\t")));

            if (alterationTypeString.equals("ADD")) {
                if (firstRow.contains(attributeNameString)) {
                    return "[ERROR] Attribute already exists: " + attributeNameString;
                }
                firstRow.add(attributeNameString);
                exsistlines.set(0, String.join("\t", firstRow));
                for (int i = 1; i < exsistlines.size(); i++) {
                    exsistlines.set(i, exsistlines.get(i) + "\t");
                }
            } else if (alterationTypeString.equals("DROP")) {
                int colIndex = firstRow.indexOf(attributeNameString);
                if (colIndex == -1) {
                    return "[ERROR] Column not found: " + attributeNameString;
                }
                firstRow.remove(colIndex);
                exsistlines.set(0, String.join("\t", firstRow));
                for (int i = 1; i < exsistlines.size(); i++) {
                    String[] values = exsistlines.get(i).split("\t");
                    List<String> newValues = new ArrayList<>(Arrays.asList(values));
                    newValues.remove(colIndex);
                    exsistlines.set(i, String.join("\t", newValues));
                }
            } else {
                return "[ERROR] Invalid ALTER operation: " + alterationTypeString;
            }
            Files.write(fileToOpen.toPath(), exsistlines);
            return "[OK]";
        } catch (IOException e) {
            return "[ERROR] Failed: " + e.getMessage();
        }
    }

}
