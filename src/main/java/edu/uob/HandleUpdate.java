
package edu.uob;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class HandleUpdate {
    private final FileManage fileManage;
    private final IsConditionValid isConditionValid;

    public HandleUpdate(FileManage fileManage, IsConditionValid isConditionValid) {
        this.fileManage = fileManage;
        this.isConditionValid = isConditionValid;
    }
    public String handleUpdate(ArrayList<String> tokens) {

        String setString = tokens.get(2).toUpperCase();

        if (!setString.equals("SET") || !tokens.contains("WHERE") || !tokens.get(4).equals("=") || tokens.size() < 6) {
            return "[ERROR] Invalid UPDATE syntax";
        }

        String tableName = tokens.get(1);
        int setIndex = tokens.indexOf("SET");
        int whereIndex = tokens.indexOf("WHERE");

        if (whereIndex == -1) {
            return "[ERROR] Missing keyword 'WHERE' ";
        }

        String setContent = String.join(" ", tokens.subList(setIndex + 1, whereIndex));
        String condition = String.join(" ", tokens.subList(whereIndex + 1, tokens.size()));

        File fileToOpen = fileManage.getTableFile(tableName);

        if (!fileManage.tableExists(fileToOpen)) {
            return "[ERROR] Table " + tableName + " does not exist" ;
        }

        try {
            List<String> existLines = fileManage.readTableFile(fileToOpen);
            if (existLines.isEmpty()) {
                return "[OK]";
            }
            List<String> header = Arrays.asList(existLines.get(0).split("\t"));
            List<String> newLines = new ArrayList<>();
            newLines.add(existLines.get(0));

            //SET
            String[] setPair = setContent.split("\\s*,\\s*");
            Map<String, String> updateValues = new HashMap<>();
            for (String pair : setPair) {
                String[] getSetValue = pair.split("\\s*=\\s*");

                if (getSetValue.length != 2) {
                    return "[ERROR] Invalid SET syntax";
                }
                updateValues.put(getSetValue[0], getSetValue[1].replace("'", ""));
            }

            boolean isUpdated = false;
            for (int i = 1; i < existLines.size(); i++) {
                String[] recordValues = existLines.get(i).split("\t");
                if (condition == null || isConditionValid.isConditionValid(condition, header, recordValues)) {

                    String[] newValues = Arrays.copyOf(recordValues, header.size());
                    for (Map.Entry<String, String> updateValue : updateValues.entrySet()) {
                        int index = header.indexOf(updateValue.getKey());
                        if (index == -1) {
                            return "[ERROR] Attribute not found";
                        }
                        newValues[index] = updateValue.getValue();
                    }
                    newLines.add(String.join("\t", newValues));
                    isUpdated = true;
                } else {
                    newLines.add(existLines.get(i));
                }
            }

            if (!isUpdated) {
                return "[OK] No data updated";
            }
            fileManage.writeTableFile(fileToOpen, newLines);
            return "[OK]";

        } catch (IOException e) {
            return "[ERROR] Failed to update: " + e.getMessage();
        }
    }

}


