package com.anyorder.pos.anyorder.modules.tables.rest;

import com.anyorder.pos.anyorder.modules.tables.model.Tables;
import com.anyorder.pos.anyorder.modules.tables.service.TablesService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tables")
@CrossOrigin(origins = "*")
public class TablesController {

    @Autowired
    private TablesService tablesService;

    @GetMapping
    public ResponseEntity<List<Tables>> getAllTables() {
        return ResponseEntity.ok(tablesService.findAll());
    }

    @GetMapping("/active")
    public ResponseEntity<List<Tables>> getActiveTables() {
        return ResponseEntity.ok(tablesService.findAllActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTableById(@PathVariable Integer id) {
        return tablesService.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Mesa no encontrada")));
    }

    @GetMapping("/area/{idArea}")
    public ResponseEntity<List<Tables>> getTablesByArea(@PathVariable Integer idArea) {
        return ResponseEntity.ok(tablesService.findByArea(idArea));
    }

    @PostMapping
    public ResponseEntity<?> createTable(@Valid @RequestBody Tables table) {
        try {
            Tables newTable = tablesService.create(table);
            return ResponseEntity.status(HttpStatus.CREATED).body(newTable);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTable(
            @PathVariable Integer id,
            @Valid @RequestBody Tables table) {
        try {
            Tables updatedTable = tablesService.update(id, table);
            return ResponseEntity.ok(updatedTable);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateTable(@PathVariable Integer id) {
        try {
            tablesService.delete(id);
            return ResponseEntity.ok(createSuccessResponse("Mesa desactivada exitosamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<?> activateTable(@PathVariable Integer id) {
        try {
            Tables table = tablesService.activate(id);
            return ResponseEntity.ok(table);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/occupancy")
    public ResponseEntity<?> updateOccupancy(
            @PathVariable Integer id,
            @RequestParam Boolean isOccupied,
            @RequestParam Integer currentOccupancy) {
        try {
            Tables table = tablesService.updateOccupancy(id, isOccupied, currentOccupancy);
            return ResponseEntity.ok(table);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        return response;
    }

    private Map<String, String> createSuccessResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }
}
