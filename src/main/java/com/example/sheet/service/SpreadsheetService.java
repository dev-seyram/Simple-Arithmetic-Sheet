package com.example.sheet.service;

import com.example.sheet.model.CellView;
import com.example.sheet.model.GridResponse;
import org.springframework.stereotype.Service;

@Service
public class SpreadsheetService {

    public static final int ROWS = 8;
    public static final int COLS = 5;

    /** Raw cell contents (formulas and values) stored in a 2D array. */
    private final String[][] data = new String[ROWS][COLS];

    public GridResponse getGrid() {
        FormulaEvaluator evaluator = new FormulaEvaluator(data);
        CellView[][] views = new CellView[ROWS][COLS];
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                String input = data[r][c] == null ? "" : data[r][c];
                String display = evaluator.evaluateDisplay(r, c);
                views[r][c] = new CellView(input, display);
            }
        }
        return new GridResponse(views, ROWS, COLS);
    }

    public GridResponse updateCell(int row, int column, String value) {
        if (row < 0 || row >= ROWS || column < 0 || column >= COLS) {
            throw new IllegalArgumentException("Row or column out of bounds");
        }
        if (value == null || value.isBlank()) {
            data[row][column] = "";
        } else {
            data[row][column] = value.trim();
        }
        return getGrid();
    }
}
