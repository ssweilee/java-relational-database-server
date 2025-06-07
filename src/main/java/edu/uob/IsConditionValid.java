
package edu.uob;

import java.io.IOException;
import java.util.List;
import java.util.Stack;

public class IsConditionValid {
   IsSimpleComparator isSimpleComparator = new IsSimpleComparator();

    public boolean isConditionValid(String condition, List<String> schema, String[] values) throws IOException {
        String[] conditionTokens = condition.trim().split("\\s+");

        if (conditionTokens.length < 3) {
            throw new IllegalArgumentException("Invalid condition syntax");
        }

        if (!condition.contains("AND") && !condition.contains("OR") && conditionTokens.length == 3) {
            return isSimpleComparator.isSimpleComparator(condition, schema, values);
        }

        Stack<Boolean> results = new Stack<>();
        Stack<String> boolOperators = new Stack<>();
        StringBuilder currentExpression = new StringBuilder();
        int depth = 0;

        for (int i = 0; i < conditionTokens.length; i++) {
            String token = conditionTokens[i];
            if (token.equals("(")) {
                depth++;
            } else if (token.equals(")")) {
                if (currentExpression.length() > 0) {
                    boolean result = isSimpleComparator.isSimpleComparator(currentExpression.toString().trim(), schema, values);
                    results.push(result);
                    currentExpression.setLength(0);
                }
                depth--;
                if (depth < 0) {
                    throw new IllegalArgumentException("Unmatched parentheses");
                }
            } else if ((token.equalsIgnoreCase("AND") || token.equalsIgnoreCase("OR")) && depth == 0) {
                if (currentExpression.length() > 0) {
                    boolean result = isSimpleComparator.isSimpleComparator(currentExpression.toString().trim(), schema, values);
                    results.push(result);
                    currentExpression.setLength(0);
                }
                boolOperators.push(token.toUpperCase());
            } else {
                currentExpression.append(token).append(" ");
                String[] exprParts = currentExpression.toString().trim().split("\\s+");
                if (exprParts.length == 3 && (i == conditionTokens.length - 1 || conditionTokens[i + 1].equals(")") || conditionTokens[i + 1].equalsIgnoreCase("AND") || conditionTokens[i + 1].equalsIgnoreCase("OR"))) {
                    boolean result = isSimpleComparator.isSimpleComparator(currentExpression.toString().trim(), schema, values);
                    results.push(result);
                    currentExpression.setLength(0);
                }
            }
        }

        if (currentExpression.length() > 0) {
            boolean result = isSimpleComparator.isSimpleComparator(currentExpression.toString().trim(), schema, values);
            results.push(result);
        }
        if (depth != 0) {
            throw new IllegalArgumentException("Unmatched parentheses in condition");
        }

        while (!boolOperators.isEmpty()) {
            if (results.size() < 2) {
                throw new IllegalArgumentException("Invalid condition syntax");
            }
            boolean right = results.pop();
            boolean left = results.pop();
            String op = boolOperators.pop();
            if (op.equals("AND")) {
                results.push(left && right);
            } else if (op.equals("OR")) {
                results.push(left || right);
            }
        }
        return results.isEmpty() ? true : results.pop();
    }

    private class IsSimpleComparator {
        private boolean isSimpleComparator(String condition, List<String> schema, String[] values) {
            String[] conditionParts = condition.trim().split("\\s+");
            if (conditionParts.length != 3) {
                throw new IllegalArgumentException("Invalid expression syntax");
            }
            int attrIndex = schema.indexOf(conditionParts[0]);
            if (attrIndex == -1)
                throw new IllegalArgumentException("Attribute not found: " + conditionParts[0]);
            String comparator = conditionParts[1];
            String value = conditionParts[2].replace("'", "");

            String existValue = values[attrIndex];

            switch (comparator) {
                case "==":
                    return existValue.equals(value);
                case ">":
                    try {
                        return Double.parseDouble(existValue) > Double.parseDouble(value);
                    } catch (NumberFormatException e) {
                        return false;
                    }
                case "<":
                    try {
                        return Double.parseDouble(existValue) < Double.parseDouble(value);
                    } catch (NumberFormatException e) {
                        return false;
                    }
                case ">=":
                    try {
                        return Double.parseDouble(existValue) >= Double.parseDouble(value);
                    } catch (NumberFormatException e) {
                        return false;
                    }
                case "<=":
                    try {
                        return Double.parseDouble(existValue) <= Double.parseDouble(value);
                    } catch (NumberFormatException e) {
                        return false;
                    }
                case "!=":
                    return !existValue.equals(value);
                case "LIKE":
                    return existValue.contains(value);
                default:
                    throw new IllegalArgumentException("Unsupported comparator: " + comparator);
            }
        }

    }
}


