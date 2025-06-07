package edu.uob;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HandleDelete {

    private final FileManage fileManage;
    private final IsConditionValid isConditionValid;

    public HandleDelete(FileManage fileManage, IsConditionValid isConditionValid) {
        this.fileManage = fileManage;
        this.isConditionValid = isConditionValid;
    }

    public String handleDelete(ArrayList<String> tokens) {
        int fromIndex = -1;
        int whereIndex = -1;
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).equalsIgnoreCase("FROM")) fromIndex = i;
            if (tokens.get(i).equalsIgnoreCase("WHERE")) whereIndex = i;
        }
        if (fromIndex == -1 || fromIndex + 1 >= tokens.size() || whereIndex == -1 || whereIndex + 3 > tokens.size() || tokens.size() < 5) {
            return "[ERROR] Invalid DELETE syntax";
        }

        String tableName = tokens.get(fromIndex + 1).toLowerCase();

        File fileToOpen = fileManage.getTableFile(tableName);
        if (!fileManage.tableExists(fileToOpen)) {
            return "[ERROR] Table does not exist: " + tableName;
        }

        try {
            String condition = String.join(" ", tokens.subList(whereIndex + 1, tokens.size()));
            List<String> existLines = fileManage.readTableFile(fileToOpen);
            List<String> firstRow = Arrays.asList(existLines.get(0).split("\t"));

            List<String> newLines = new ArrayList<>();
            newLines.add(existLines.get(0));

            boolean isDeleted = false;
            for (int i = 1; i < existLines.size(); i++) {
                String[] lineValues = existLines.get(i).split("\t");
                if (!isConditionValid.isConditionValid(condition, firstRow, lineValues)) {
                    newLines.add(existLines.get(i));
                } else {
                    isDeleted = true;
                }
            }

            if (!isDeleted) {
                return "[ERROR]: Nothing matched";
            }
            Files.write(fileToOpen.toPath(), newLines);
            return "[OK]";
        } catch (IOException e) {
            return "[ERROR] Failed to delete: " + e.getMessage();
        }
    }
}
