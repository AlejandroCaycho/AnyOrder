package com.anyorder.pos.anyorder.modules.areas.rest;

import com.anyorder.pos.anyorder.modules.areas.model.Area;
import com.anyorder.pos.anyorder.modules.areas.service.AreaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/areas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AreaController {

    private final AreaService areaService;

    @GetMapping
    public ResponseEntity<List<Area>> getAll() {
        return ResponseEntity.ok(areaService.findAll());
    }

    @GetMapping("/active")
    public ResponseEntity<List<Area>> getActive() {
        return ResponseEntity.ok(areaService.findAllActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        return areaService.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Área no encontrada")));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Area>> search(@RequestParam String name) {
        return ResponseEntity.ok(areaService.searchByName(name));
    }

    @GetMapping("/stats")
    public ResponseEntity<List<Map<String, Object>>> getStats() {
        return ResponseEntity.ok(areaService.getAreaStats());
    }

    @GetMapping("/count/active")
    public ResponseEntity<Map<String, Long>> countActive() {
        Map<String, Long> response = new HashMap<>();
        response.put("count", areaService.countActive());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Area> create(@Valid @RequestBody Area area) {
        return ResponseEntity.status(HttpStatus.CREATED).body(areaService.create(area));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Area> update(@PathVariable Integer id, @Valid @RequestBody Area area) {
        return ResponseEntity.ok(areaService.update(id, area));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivate(@PathVariable Integer id) {
        areaService.delete(id);
        return ResponseEntity.ok(createSuccessResponse("Área desactivada exitosamente"));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Area> activate(@PathVariable Integer id) {
        return ResponseEntity.ok(areaService.activate(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> hardDelete(@PathVariable Integer id) {
        areaService.hardDelete(id);
        return ResponseEntity.ok(createSuccessResponse("Área eliminada permanentemente"));
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
