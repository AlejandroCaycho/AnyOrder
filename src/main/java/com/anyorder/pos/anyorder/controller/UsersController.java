package com.anyorder.pos.anyorder.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.anyorder.pos.anyorder.model.Users;
import com.anyorder.pos.anyorder.service.UsersService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class UsersController {

    private final UsersService userService;

    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody Users user) {
        try {
            Users createdUser = userService.createUser(user);
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Users>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/active")
    public ResponseEntity<List<Users>> getActiveUsers() {
        return ResponseEntity.ok(userService.getActiveUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Integer id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    @GetMapping("/document/{documentNumber}")
    public ResponseEntity<?> getUserByDocument(@PathVariable String documentNumber) {
        return userService.getUserByDocumentNumber(documentNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Users>> searchUsers(@RequestParam String query) {
        return ResponseEntity.ok(userService.searchUsersByName(query));
    }

    @GetMapping("/role/{roleId}")
    public ResponseEntity<List<Users>> getUsersByRole(@PathVariable Integer roleId) {
        return ResponseEntity.ok(userService.getUsersByRole(roleId));
    }

    @GetMapping("/area/{areaId}")
    public ResponseEntity<List<Users>> getUsersByArea(@PathVariable Integer areaId) {
        return ResponseEntity.ok(userService.getUsersByArea(areaId));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Integer id,
            @Valid @RequestBody Users user) {
        try {
            return ResponseEntity.ok(userService.updateUser(id, user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/deactivate/{id}")
    public ResponseEntity<?> deactivateUser(@PathVariable Integer id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok(Map.of("message", "Usuario desactivado"));
    }

    @PatchMapping("/activate/{id}")
    public ResponseEntity<?> activateUser(@PathVariable Integer id) {
        userService.activateUser(id);
        return ResponseEntity.ok(Map.of("message", "Usuario activado"));
    }

    @DeleteMapping("/physical/{id}")
    public ResponseEntity<?> physicalDeleteUser(@PathVariable Integer id) {
        userService.physicalDeleteUser(id);
        return ResponseEntity.ok(Map.of("message", "Usuario eliminado"));
    }

    @GetMapping("/exists/email/{email}")
    public ResponseEntity<Map<String, Boolean>> existsByEmail(@PathVariable String email) {
        return ResponseEntity.ok(Map.of("exists", userService.existsByEmail(email)));
    }

    @GetMapping("/exists/document/{documentNumber}")
    public ResponseEntity<Map<String, Boolean>> existsByDocument(@PathVariable String documentNumber) {
        return ResponseEntity.ok(Map.of("exists", userService.existsByDocumentNumber(documentNumber)));
    }

    // =========================
    // FOTO PERFIL (LOCAL)
    // =========================
    @PostMapping("/profile-photo/{id}")
    public ResponseEntity<?> uploadProfilePhoto(
            @PathVariable Integer id,
            @RequestParam("file") MultipartFile file) {

        return ResponseEntity.ok(userService.uploadProfilePhoto(id, file));
    }

    @DeleteMapping("/profile-photo-delete/{id}")
    public ResponseEntity<?> deleteProfilePhoto(@PathVariable Integer id) {
        userService.deleteProfilePhoto(id);
        return ResponseEntity.ok(Map.of("message", "Foto eliminada"));
    }
}
