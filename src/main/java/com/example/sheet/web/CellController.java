package com.example.sheet.web;

import com.example.sheet.model.CellUpdateRequest;
import com.example.sheet.model.GridResponse;
import com.example.sheet.service.SpreadsheetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/cells")
public class CellController {

    private final SpreadsheetService spreadsheetService;

    public CellController(SpreadsheetService spreadsheetService) {
        this.spreadsheetService = spreadsheetService;
    }

    @GetMapping
    public GridResponse getCells() {
        return spreadsheetService.getGrid();
    }

    @PostMapping("/update")
    public GridResponse updateCell(@RequestBody CellUpdateRequest request) {
        return spreadsheetService.updateCell(request.getRow(), request.getColumn(), request.getValue());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }
}
