package com.anyorder.pos.anyorder.modules.users.rest;

import com.anyorder.pos.anyorder.modules.users.model.UserLog;
import com.anyorder.pos.anyorder.modules.users.service.UserLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user-logs")
@CrossOrigin(origins = "*")
public class UserLogController {

    @Autowired
    private UserLogService userLogService;

    @GetMapping
    public ResponseEntity<List<UserLog>> getAllLogs() {
        return ResponseEntity.ok(userLogService.findAll());
    }

    @GetMapping("/user/{idUser}")
    public ResponseEntity<List<UserLog>> getLogsByUser(@PathVariable Integer idUser) {
        return ResponseEntity.ok(userLogService.findByUserId(idUser));
    }

    @GetMapping("/user/{idUser}/active")
    public ResponseEntity<UserLog> getActiveLog(@PathVariable Integer idUser) {
        return userLogService.findActiveLog(idUser)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{idUser}/earnings")
    public ResponseEntity<Map<String, BigDecimal>> getTotalEarnings(@PathVariable Integer idUser) {
        return ResponseEntity.ok(Map.of("totalEarnings", userLogService.calculateTotalEarnings(idUser)));
    }

    @PostMapping("/start/{idUser}")
    public ResponseEntity<?> startLog(@PathVariable Integer idUser) {
        try {
            return ResponseEntity.ok(userLogService.startLog(idUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/end/{idUser}")
    public ResponseEntity<?> endLog(@PathVariable Integer idUser) {
        try {
            return ResponseEntity.ok(userLogService.endLog(idUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
