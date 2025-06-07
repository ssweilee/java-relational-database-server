package edu.uob;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.*;

/** This class implements the DB server. */
public class DBServer {

    private static final char END_OF_TRANSMISSION = 4;
    private String storageFolderPath = Paths.get("databases").toAbsolutePath().toString();
    private String currentDatabase;
    private FileManage fileManage;
    private HandleUpdate handleUpdate;
    private IsConditionValid isConditionValid;
    private HandleSelect handleSelect;
    private HandleDelete handleDelete;
    private HandleAlter handleAlter;
    private HandleDrop handleDrop;
    private HandleJoin handleJoin;
    private HandleInsert handleInsert;
    private HandleCreate handleCreate;
    private HandleUse handleUse;
    private HandleTokens handleTokens;

    public static void main(String args[]) throws IOException {
        DBServer server = new DBServer();
        server.start();
        server.blockingListenOn(8888);
    }

    /**
    * KEEP this signature otherwise we won't be able to mark your submission correctly.
    */
    public DBServer() {

        try {
            // Create the database storage folder if it doesn't already exist !
            Files.createDirectories(Paths.get(storageFolderPath));
            this.currentDatabase = null;

            this.isConditionValid = new IsConditionValid();
            this.fileManage = new FileManage(storageFolderPath);
            this.handleUpdate = new HandleUpdate(fileManage, isConditionValid);
            this.handleSelect = new HandleSelect(fileManage, isConditionValid);
            this.handleDelete = new HandleDelete(fileManage, isConditionValid);
            this.handleAlter = new HandleAlter(fileManage);
            this.handleDrop = new HandleDrop(fileManage);
            this.handleJoin = new HandleJoin(fileManage);
            this.handleInsert = new HandleInsert(fileManage);
            this.handleCreate = new HandleCreate(fileManage);
            this.handleUse = new HandleUse();
            this.handleTokens = new HandleTokens();

        } catch(IOException ioe) {
            System.out.println("Can't seem to create database storage folder " + storageFolderPath);
        }
    }

    public void start() {
        this.fileManage.setDBServer(this);
        this.handleCreate.setDBServer(this);
        this.handleUse.setDBServer(this);
    }

    public void setCurrentDatabase(String db) {
        this.currentDatabase = db.toLowerCase();
        this.fileManage.setDBServer(this);
        this.handleCreate.setDBServer(this);
        this.handleUse.setDBServer(this);
    }

    public String getStorageFolderPath() {
        return storageFolderPath;
    }

    public String getCurrentDatabase() {
        return currentDatabase;
    }

    /**
    * KEEP this signature (i.e. {@code edu.uob.DBServer.handleCommand(String)}) otherwise we won't be
    * able to mark your submission correctly.
    *
    * <p>This method handles all incoming DB commands and carries out the required actions.
    */
    public String handleCommand(String command) {
        // TODO implement your server logic here
        ArrayList<String> tokens = handleTokens.tokeniseCommand(command);
        String commandType = tokens.get(0).toUpperCase();

        if (getCurrentDatabase() == null && !commandType.equals("USE") && !(commandType.equals("CREATE") && tokens.get(1).toUpperCase().equals("DATABASE"))) {
            return "[ERROR] No database selected";
        }

        //Preprocessing tokens
        if (tokens.isEmpty() || !tokens.get(tokens.size() - 1).equals(";")) {
            return "[ERROR] Semi colon missing at end of line";
        }
        tokens.remove(tokens.size() - 1);

        this.fileManage.setDBServer(this);
        this.handleCreate.setDBServer(this);
        this.handleUse.setDBServer(this);

        //Identifying command type
        try {
            switch (commandType) {
                case "CREATE":
                    return handleCreate.handleCreate(tokens);
                case "USE":
                    return handleUse.handleUse(tokens);
                case "INSERT":
                    return handleInsert.handleInsert(tokens);
                case "SELECT":
                    return handleSelect.handleSelect(tokens);
                case "UPDATE":
                    return handleUpdate.handleUpdate(tokens);
                case "DELETE":
                    return handleDelete.handleDelete(tokens);
                case "ALTER":
                    return handleAlter.handleAlter(tokens);
                case "DROP":
                    return handleDrop.handleDrop(tokens);
                case "JOIN":
                    return handleJoin.handleJoin(tokens);
                default:
                    return "[ERROR] Unknown command: " + commandType;
            }
        } catch (Exception e) {
            return "[ERROR] " + e.getMessage();
        }
    }


    //  === Methods below handle networking aspects of the project - you will not need to change these ! ===

    public void blockingListenOn(int portNumber) throws IOException {
        try (ServerSocket s = new ServerSocket(portNumber)) {
            System.out.println("Server listening on port " + portNumber);
            while (!Thread.interrupted()) {
                try {
                    blockingHandleConnection(s);
                } catch (IOException e) {
                    System.err.println("Server encountered a non-fatal IO error:");
                    e.printStackTrace();
                    System.err.println("Continuing...");
                }
            }
        }
    }

    private void blockingHandleConnection(ServerSocket serverSocket) throws IOException {
        try (Socket s = serverSocket.accept();
        BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {

            System.out.println("Connection established: " + serverSocket.getInetAddress());
            while (!Thread.interrupted()) {
                String incomingCommand = reader.readLine();
                System.out.println("Received message: " + incomingCommand);
                String result = handleCommand(incomingCommand);
                writer.write(result);
                writer.write("\n" + END_OF_TRANSMISSION + "\n");
                writer.flush();
            }
        }
    }
}
