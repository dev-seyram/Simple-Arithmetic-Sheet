package com.example.sheet.service;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Evaluates spreadsheet formulas and arithmetic expressions.
 */
public class FormulaEvaluator {

    private static final Pattern CELL_REF = Pattern.compile("([A-E])([1-8])", Pattern.CASE_INSENSITIVE);
    private static final int ROWS = 8;
    private static final int COLS = 5;

    private final String[][] data;

    public FormulaEvaluator(String[][] data) {
        this.data = data;
    }

    public String evaluateDisplay(int row, int col) {
        return evaluateDisplay(row, col, new HashSet<>());
    }

    private String evaluateDisplay(int row, int col, Set<String> visiting) {
        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) {
            return "#REF!";
        }
        String key = row + "," + col;
        if (visiting.contains(key)) {
            return "#CYCLE!";
        }
        visiting.add(key);

        String raw = data[row][col];
        if (raw == null || raw.isBlank()) {
            visiting.remove(key);
            return "";
        }

        String trimmed = raw.trim();
        if (trimmed.startsWith("=")) {
            try {
                String expr = trimmed.substring(1).trim();
                if (expr.isEmpty()) {
                    visiting.remove(key);
                    return "";
                }
                double result = evaluateExpression(expr, visiting);
                visiting.remove(key);
                return formatNumber(result);
            } catch (EvaluationException e) {
                visiting.remove(key);
                return e.getMessage();
            }
        }

        visiting.remove(key);
        if (isNumeric(trimmed)) {
            return formatNumber(Double.parseDouble(trimmed));
        }
        return trimmed;
    }

    private double evaluateExpression(String expr, Set<String> visiting) {
        String substituted = substituteCellRefs(expr, visiting);
        return parseArithmetic(substituted);
    }

    private String substituteCellRefs(String expr, Set<String> visiting) {
        Matcher m = CELL_REF.matcher(expr);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            int col = m.group(1).toUpperCase().charAt(0) - 'A';
            int row = Integer.parseInt(m.group(2)) - 1;
            String display = evaluateDisplay(row, col, visiting);
            if (display.startsWith("#") || display.isEmpty()) {
                throw new EvaluationException(display.isEmpty() ? "#EMPTY!" : display);
            }
            try {
                double num = Double.parseDouble(display);
                m.appendReplacement(sb, Matcher.quoteReplacement(Double.toString(num)));
            } catch (NumberFormatException e) {
                throw new EvaluationException("#VALUE!");
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * Parses +, -, *, / with standard precedence (* / before + -).
     */
    private double parseArithmetic(String expr) {
        expr = expr.replaceAll("\\s+", "");
        if (expr.isEmpty()) {
            throw new EvaluationException("#EMPTY!");
        }
        return parseAddSubtract(expr, new int[]{0});
    }

    private double parseAddSubtract(String expr, int[] pos) {
        double left = parseMultiplyDivide(expr, pos);
        while (pos[0] < expr.length()) {
            char op = expr.charAt(pos[0]);
            if (op != '+' && op != '-') {
                break;
            }
            pos[0]++;
            double right = parseMultiplyDivide(expr, pos);
            if (op == '+') {
                left += right;
            } else {
                left -= right;
            }
        }
        return left;
    }

    private double parseMultiplyDivide(String expr, int[] pos) {
        double left = parseUnary(expr, pos);
        while (pos[0] < expr.length()) {
            char op = expr.charAt(pos[0]);
            if (op != '*' && op != '/') {
                break;
            }
            pos[0]++;
            double right = parseUnary(expr, pos);
            if (op == '*') {
                left *= right;
            } else {
                if (right == 0) {
                    throw new EvaluationException("#DIV/0!");
                }
                left /= right;
            }
        }
        return left;
    }

    private double parseUnary(String expr, int[] pos) {
        if (pos[0] < expr.length() && expr.charAt(pos[0]) == '-') {
            pos[0]++;
            return -parsePrimary(expr, pos);
        }
        if (pos[0] < expr.length() && expr.charAt(pos[0]) == '+') {
            pos[0]++;
        }
        return parsePrimary(expr, pos);
    }

    private double parsePrimary(String expr, int[] pos) {
        if (pos[0] < expr.length() && expr.charAt(pos[0]) == '(') {
            pos[0]++;
            double val = parseAddSubtract(expr, pos);
            if (pos[0] >= expr.length() || expr.charAt(pos[0]) != ')') {
                throw new EvaluationException("#PARSE!");
            }
            pos[0]++;
            return val;
        }
        return parseNumber(expr, pos);
    }

    private double parseNumber(String expr, int[] pos) {
        int start = pos[0];
        if (pos[0] < expr.length() && (expr.charAt(pos[0]) == '.' || Character.isDigit(expr.charAt(pos[0])))) {
            while (pos[0] < expr.length()) {
                char c = expr.charAt(pos[0]);
                if (Character.isDigit(c) || c == '.') {
                    pos[0]++;
                } else {
                    break;
                }
            }
        } else {
            throw new EvaluationException("#PARSE!");
        }
        try {
            return Double.parseDouble(expr.substring(start, pos[0]));
        } catch (NumberFormatException e) {
            throw new EvaluationException("#PARSE!");
        }
    }

    private static boolean isNumeric(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static String formatNumber(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "#NUM!";
        }
        if (value == Math.rint(value) && Math.abs(value) < 1e15) {
            return Long.toString((long) value);
        }
        return Double.toString(value);
    }

    private static class EvaluationException extends RuntimeException {
        EvaluationException(String message) {
            super(message);
        }
    }
}
