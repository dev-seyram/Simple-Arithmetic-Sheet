package com.example.sheet.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FormulaEvaluatorTest {

    @Test
    void addsCellReferences() {
        String[][] data = new String[8][5];
        data[0][0] = "10";
        data[0][1] = "20";
        data[0][2] = "=A1+B1";
        FormulaEvaluator ev = new FormulaEvaluator(data);
        assertEquals("30", ev.evaluateDisplay(0, 2));
    }

    @Test
    void respectsOperatorPrecedence() {
        String[][] data = new String[8][5];
        data[0][0] = "=2+3*4";
        FormulaEvaluator ev = new FormulaEvaluator(data);
        assertEquals("14", ev.evaluateDisplay(0, 0));
    }

    @Test
    void detectsCycles() {
        String[][] data = new String[8][5];
        data[0][0] = "=B1";
        data[0][1] = "=A1";
        FormulaEvaluator ev = new FormulaEvaluator(data);
        assertEquals("#CYCLE!", ev.evaluateDisplay(0, 0));
    }
}
