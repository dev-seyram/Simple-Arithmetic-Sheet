package com.example.sheet.model;

public class GridResponse {
    private CellView[][] cells;
    private int rows;
    private int columns;

    public GridResponse(CellView[][] cells, int rows, int columns) {
        this.cells = cells;
        this.rows = rows;
        this.columns = columns;
    }

    public CellView[][] getCells() {
        return cells;
    }

    public void setCells(CellView[][] cells) {
        this.cells = cells;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }
}
