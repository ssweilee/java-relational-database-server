package edu.uob;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;
import java.nio.file.Path;

public class FileManage {
    private String storageFolderPath;
    private DBServer dbServer;

    public FileManage(String storageFolderPath) {
        this.storageFolderPath = storageFolderPath;
    }

    public void setDBServer(DBServer dbServer) {
        this.dbServer = dbServer;
    }

    public File getTableFile(String tableName) throws IllegalArgumentException {
        String currentDatabase = dbServer.getCurrentDatabase();
        if (currentDatabase == null) {
            throw new IllegalArgumentException("[ERROR] No selected database");
        }

        return new File(storageFolderPath + File.separator + currentDatabase, tableName + ".tab");
    }

    public List<String> readTableFile(File tableFile) throws IOException {
        if (!tableFile.exists()) {
            throw new IllegalArgumentException("Table file does not exist");
        }
        return Files.readAllLines(tableFile.toPath());
    }

    public int readIdHistoryFile(File idHistoryFile) throws IOException {
        if (!idHistoryFile.exists()) return 0;
        List<String> lines = Files.readAllLines(idHistoryFile.toPath());
        for (String line : lines) {
            if (line.startsWith("maxId=")) {
                return Integer.parseInt(line.split("=")[1]);
            }
        }
        throw new IOException("Invalid format in " + idHistoryFile.getName());
    }

    public void writeTableFile(File tableFile, List<String> lines) throws IOException {
        Files.write(tableFile.toPath(), lines);
    }

    public void deleteTable(String tableName) throws IOException {
        File tableFile = getTableFile(tableName);
        if (!tableFile.exists()) {
            throw new IOException("[ERROR] Table does not exist: " + tableName);
        }
        if (!tableFile.delete()) {
            throw new IOException("[ERROR] Delete table: " + tableName + " failed");
        }
    }

    public void deleteDatabase(String dbName) throws IOException {
        File databaseDir = new File(storageFolderPath, dbName);
        if (!databaseDir.exists() || !databaseDir.isDirectory()) {
            throw new IOException("[ERROR] Database does not exist: " + dbName);
        }
        deleteDirectory(databaseDir.toPath());
    }

    private void deleteDirectory(Path dir) throws IOException {
        Files.walk(dir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    public void createDatabase(String dbName) throws IOException {
        File dbRoot = new File("databases");
        if (!dbRoot.exists() && !dbRoot.mkdir()) {
            throw new IOException ("[ERROR] Failed to create root database directory!");
        }

        File dbFolder = new File(storageFolderPath, dbName);
        if (dbFolder.exists()) {
            throw new IOException("Database " + dbName + " already exists");
        }
        if(!dbFolder.mkdirs()) {
            throw new IOException("[ERROR] Unable to create database folder " + dbName);
        }
    }

    public boolean tableExists(File tableFile) {
        return tableFile.exists();
    }
}