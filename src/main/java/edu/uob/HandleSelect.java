package edu.uob;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HandleSelect {
    private final FileManage fileManage;
    private final IsConditionValid isConditionValid;

    public HandleSelect(FileManage fileManage, IsConditionValid isConditionValid) {
        this.fileManage = fileManage;
        this.isConditionValid = isConditionValid;
    }

    public String handleSelect(ArrayList<String> tokens) {
        int fromIndex = -1;
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).equalsIgnoreCase("FROM")) {
                fromIndex = i;
                break;
            }
        }
        if (fromIndex == -1 || fromIndex + 1 >= tokens.size()) {
            return "[ERROR] Missing Keyword FROM or table name";
        }

        String wildAttribs = String.join(" ", tokens.subList(1, fromIndex));
        String restPart = String.join(" ", tokens.subList(fromIndex + 1, tokens.size()));
        String tablePart;
        String condition;

        int whereIndex = tokens.indexOf("WHERE");
        if (restPart.contains(" WHERE ")) {
            tablePart = String.join(" ", tokens.subList(fromIndex + 1, whereIndex));
            condition = restPart.substring(restPart.indexOf(" WHERE ") + 7);

        } else {
            tablePart = String.join(" ", tokens.subList(fromIndex + 1, tokens.size()));
            condition =  null;
        }


        try {
            String.join(" ", tokens.subList(fromIndex + 1, tokens.size()));
            String tableName = tablePart.split(" ")[0];


            File fileToOpen = fileManage.getTableFile(tableName);
            if (!fileManage.tableExists(fileToOpen)) {
                return "[ERROR] Table does not exist: " + tableName;
            }

            List<String> lines = fileManage.readTableFile(fileToOpen);
            if (lines.isEmpty()) {
                return "[OK]\n" + wildAttribs;
            }
            List<String> header = Arrays.asList(lines.get(0).split("\t"));
            List<String> result = new ArrayList<>();
            result.add("[OK]");

            if (wildAttribs.equals("*")) {
                if (!lines.isEmpty()) {
                    result.add(lines.get(0));
                }
            } else {
                List<String> attrs = Arrays.asList(wildAttribs.split("\\s*,\\s*"));
                List<String> selectedAttr = new ArrayList<>();
                for (String attr : attrs) {
                    if (!header.contains(attr)) return "[ERROR] Attribute does not exist";
                    selectedAttr.add(attr);
                }
                result.add(String.join("\t", selectedAttr));
            }

            for (int i = 1; i < lines.size(); i++) {
                String[] existValues = lines.get(i).split("\t");
                String[] paddedValues = Arrays.copyOf(existValues, header.size());
                for (int j = existValues.length; j < header.size(); j++) {
                    paddedValues[j] = "";
                }

                if (condition == null || isConditionValid.isConditionValid(condition, header, existValues)) {
                    if (wildAttribs.equals("*")) {
                        result.add(lines.get(i));
                    } else {
                        List<String> attrs = Arrays.asList(wildAttribs.split("\\s*,\\s*"));
                        List<String> selected = new ArrayList<>();
                        for (String attr : attrs) {
                            int idx = header.indexOf(attr);
                            selected.add(existValues[idx]);
                        }
                        result.add(String.join("\t", selected));
                    }
                }
            }
            return String.join("\n", result);

        } catch (IOException e) {
            return "[ERROR] Table does not exist";
        }
    }
}
