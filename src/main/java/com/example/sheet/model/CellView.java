package com.example.sheet.model;

public class CellView {
    private String input;
    private String display;

    public CellView() {
    }

    public CellView(String input, String display) {
        this.input = input;
        this.display = display;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }
}
