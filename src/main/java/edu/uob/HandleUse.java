package edu.uob;

import java.io.File;
import java.util.ArrayList;

public class HandleUse {

    private DBServer dbServer;

    public HandleUse() {
    }

    public void setDBServer(DBServer dbServer) {
        this.dbServer = dbServer;
    }

    public String handleUse(ArrayList<String> tokens) {
        if (tokens.size() < 2) {
            return "[ERROR] Missing database name";
        }
        String dbName = tokens.get(1).toLowerCase();
        dbServer.setCurrentDatabase(dbName);

        File dbFolder = new File(dbServer.getStorageFolderPath(), dbName);

        if (!dbFolder.exists() || !dbFolder.isDirectory()) {
            return "[ERROR] Database does not exist: " + dbName;
        }
        dbServer.setCurrentDatabase(dbName);
        return "[OK]";
    }
}
