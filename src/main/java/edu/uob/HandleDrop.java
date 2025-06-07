package edu.uob;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class HandleDrop {

    private final FileManage fileManage;

    public HandleDrop(FileManage fileManage) {
        this.fileManage = fileManage;
    }
    public String handleDrop(ArrayList<String> tokens) {
        if (tokens.size() < 3) return "[ERROR] Invalid DROP syntax";

        String dbOrTable = tokens.get(1).toUpperCase();
        String nameString = tokens.get(2).toLowerCase();
        File fileToOpen = fileManage.getTableFile(nameString);
        File idHistoryFile = new File(fileToOpen.getParent(), nameString + "_idHistory.txt");

        try {
            if (dbOrTable.equals("DATABASE")) {
                fileManage.deleteDatabase(nameString);
                if (idHistoryFile.exists()) {
                    idHistoryFile.delete();
                }
                return "[OK] Database '" + nameString.toLowerCase() + "' deleted";
            } else if (dbOrTable.equals("TABLE")) {
               fileManage.deleteTable(nameString);
                if (idHistoryFile.exists()) {
                    idHistoryFile.delete();
                }
                return "[OK] Table '" + nameString.toLowerCase() + "' deleted";
            } else {
                return "[ERROR] Invalid DROP syntax: "+ dbOrTable + " must be DATABASE or TABLE";
            }
        } catch (IOException e) {
            return e.getMessage();
        }
    }
}