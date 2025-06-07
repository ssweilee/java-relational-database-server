package edu.uob;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HandleJoin {

    private final FileManage fileManage;

    public HandleJoin(FileManage fileManage) {
        this.fileManage = fileManage;
    }
    public String handleJoin(ArrayList<String> tokens) {
        if (!tokens.get(2).equalsIgnoreCase("AND") || !tokens.get(4).equalsIgnoreCase("ON") || !tokens.get(6).equalsIgnoreCase("AND") || tokens.size() < 7) {
            return "[ERROR] Invalid JOIN syntax";
        }
        String table1 = tokens.get(1);
        String table2 = tokens.get(3);
        String attr1 = tokens.get(5);
        String attr2 = tokens.get(7);


        File file1ToOpen = fileManage.getTableFile(table1);
        File file2ToOpen = fileManage.getTableFile(table2);

        try {
            List<String> existlines1 = fileManage.readTableFile(file1ToOpen);
            List<String> existlines2 = fileManage.readTableFile(file2ToOpen);
            List<String> firstRow1 = Arrays.asList(existlines1.get(0).split("\t"));
            List<String> firstRow2 = Arrays.asList(existlines2.get(0).split("\t"));

            int attr1Index = firstRow1.indexOf(attr1);
            int attr2Index = firstRow2.indexOf(attr2);
            if (attr1Index == -1 || attr2Index == -1) {
                return "[ERROR] Attribute not found";
            }

            List<String> result = new ArrayList<>();
            result.add("[OK]");
            List<String> newSchema = new ArrayList<>();
            newSchema.add("id");
            for (int i = 1; i < firstRow1.size(); i++) {
                if (i != attr1Index) newSchema.add(table1 + "." + firstRow1.get(i));
            }
            for (int i = 1; i < firstRow2.size(); i++) {
                if (i != attr2Index) newSchema.add(table2 + "." + firstRow2.get(i));
            }
            result.add(String.join("\t", newSchema));

            int newId = 1;
            for (int i = 1; i < existlines1.size(); i++) {
                String[] existvalues1 = existlines1.get(i).split("\t");
                for (int j = 1; j < existlines2.size(); j++) {
                    String[] existvalues2 = existlines2.get(j).split("\t");
                    if (existvalues1[attr1Index].equals(existvalues2[attr2Index])) {
                        List<String> row = new ArrayList<>();
                        row.add(String.valueOf(newId++));
                        for (int k = 1; k < existvalues1.length; k++) {
                            if (k != attr1Index) row.add(existvalues1[k]);
                        }
                        for (int k = 1; k < existvalues2.length; k++) {
                            if (k != attr2Index) row.add(existvalues2[k]);
                        }
                        result.add(String.join("\t", row));
                    }
                }
            }

            return String.join("\n", result);
        } catch (IOException e) {
            return "[ERROR] Failed to join tables: " + e.getMessage();
        }
    }
}
