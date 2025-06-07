package edu.uob;

import java.util.ArrayList;
import java.util.Arrays;

public class HandleTokens {
    String[] specialCharacters = {"(", ")", ",", ";", ">", "<", "==", "!=", ">=", "<="};

    public ArrayList<String> tokeniseCommand(String command) {

        ArrayList<String> tokens = new ArrayList<>();
        String[] token = command.split("'");
        for (int i = 0; i < token.length; i++) {
            if (i % 2 != 0) {
                tokens.add("'" + token[i] + "'");
            }
            else {
                String[] nextUsefulTokens = tokenise(token[i]);
                tokens.addAll(Arrays.asList(nextUsefulTokens));
            }
        }

        return tokens;
    }

    private String[] tokenise(String input) {
        for (String specialCharacter : specialCharacters) {
            input = input.replace(specialCharacter, " " + specialCharacter + " ");
        }

        while (input.contains("  ")) input = input.replace("  ", " ");
        input = input.trim();
        return input.split(" ");
    }
}
