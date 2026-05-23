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
    public ResponseEntity<Tables> createTable(@Valid @RequestBody Tables table) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tablesService.create(table));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tables> updateTable(@PathVariable Integer id, @Valid @RequestBody Tables table) {
        return ResponseEntity.ok(tablesService.update(id, table));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateTable(@PathVariable Integer id) {
        tablesService.delete(id);
        return ResponseEntity.ok(createSuccessResponse("Mesa desactivada exitosamente"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> hardDeleteTable(@PathVariable Integer id) {
        tablesService.hardDelete(id);
        return ResponseEntity.ok(createSuccessResponse("Mesa eliminada permanentemente"));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Tables> activateTable(@PathVariable Integer id) {
        return ResponseEntity.ok(tablesService.activate(id));
    }

    @PatchMapping("/{id}/occupancy")
    public ResponseEntity<Tables> updateOccupancy(
            @PathVariable Integer id,
            @RequestParam Boolean isOccupied,
            @RequestParam Integer currentOccupancy) {
        return ResponseEntity.ok(tablesService.updateOccupancy(id, isOccupied, currentOccupancy));
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
