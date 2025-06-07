package edu.uob;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HandleCreate {

    private FileManage fileManage;
    private DBServer dbServer;

    public HandleCreate(FileManage fileManage) {
        this.fileManage = fileManage;
    }

    public void setDBServer(DBServer dbServer) {
        this.dbServer = dbServer;
    }

    public String handleCreate(ArrayList<String> tokens) throws IOException {
        if (tokens.size() < 3) {
            return "[ERROR] Invalid CREATE syntax";
        }
        String type = tokens.get(1).toLowerCase();
        if (type.toUpperCase().equals("DATABASE")) {
            String dbName = tokens.get(2).toLowerCase();
            try {
                fileManage.createDatabase(dbName);
                dbServer.setCurrentDatabase(dbName.toLowerCase());
                return "[OK]";
            } catch (IOException e) {
                return "[ERROR] " + (e.getMessage() != null ? e.getMessage() : "Failed to create database: " + dbName);
            }
        } else if (type.toUpperCase().equals("TABLE")) {

            //Set Table name
            if (dbServer.getCurrentDatabase() == null) {
                return "[ERROR] No selected database";
            }
            String tableName = tokens.get(2).toLowerCase();

            File fileToOpen = fileManage.getTableFile(tableName);

            if (fileManage.tableExists(fileToOpen)) {
                return "[ERROR] Table " + tableName + " already exists";
            }
            List<String> attributeList = new ArrayList<>();

            //If there are attributes
            if (tokens.size() > 3) {
                String attrs = String.join(" ", tokens.subList(3, tokens.size()));
                if (!attrs.startsWith("(") || !attrs.endsWith(")")) {
                    return "[ERROR] Invalid attribute syntax: missing parentheses";
                }
                attrs = attrs.substring(1, attrs.length() - 1);
                attributeList = Arrays.asList(attrs.split(","));
                for (int i = 0; i < attributeList.size(); i++) {
                    attributeList.set(i, attributeList.get(i).trim());
                }
                attributeList = new ArrayList<>(attributeList);

            }
            attributeList.add(0, "id");

            if (fileToOpen.createNewFile()) {
                try (BufferedWriter writer = Files.newBufferedWriter(fileToOpen.toPath())) {
                    writer.write(String.join("\t", attributeList));
                    writer.newLine();
                } catch (IOException e) {
                    return "[ERROR] Failed to create table: " + e.getMessage();
                }
                return "[OK]";
            } else {
                return "[ERROR] Failed to create table file: " + fileToOpen.getAbsolutePath();
            }
        } else {
            return "[ERROR] Invalid CREATE type: must be DATABASE or TABLE";
        }

    }
}
