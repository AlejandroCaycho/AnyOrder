package com.anyorder.pos.anyorder.modules.sunat.rest;

import com.anyorder.pos.anyorder.modules.sunat.model.SunatData;
import com.anyorder.pos.anyorder.modules.sunat.service.SunatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sunat")
@CrossOrigin(origins = "*")
public class SunatController {

    private final SunatService sunatService;

    public SunatController(SunatService sunatService) {
        this.sunatService = sunatService;
    }

    @GetMapping("/ruc/{ruc}")
    public ResponseEntity<?> consultarRuc(@PathVariable String ruc) {
        try {
            SunatData data = sunatService.consultarRuc(ruc);
            if (data != null) {
                return ResponseEntity.ok(data);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
