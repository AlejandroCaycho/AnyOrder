package com.anyorder.pos.anyorder.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.anyorder.pos.anyorder.model.Users;
import com.anyorder.pos.anyorder.repository.UsersRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsersService {

    private final UsersRepository userRepository;

    private static final String UPLOAD_DIR = "src/main/resources/static/usuarios";

    // =========================
    // CRUD
    // =========================
    @Transactional
    public Users createUser(Users user) {

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        if (userRepository.existsByDocumentNumber(user.getDocumentNumber())) {
            throw new RuntimeException("El documento ya está registrado");
        }

        if (!user.getHoraFin().isAfter(user.getHoraInicio())) {
            throw new RuntimeException("Hora fin debe ser mayor a inicio");
        }

        if (user.getState() == null) {
            user.setState(true);
        }

        return userRepository.save(user);
    }

    @Transactional
    public Users updateUser(Integer id, Users user) {

        Users existing = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        existing.setName(user.getName());
        existing.setSurnames(user.getSurnames());
        existing.setEmail(user.getEmail());
        existing.setDocumentNumber(user.getDocumentNumber());
        existing.setDocumentType(user.getDocumentType());
        existing.setGender(user.getGender());
        existing.setPhone(user.getPhone());
        existing.setAddress(user.getAddress());
        existing.setRole(user.getRole());
        existing.setArea(user.getArea());
        existing.setHoraInicio(user.getHoraInicio());
        existing.setHoraFin(user.getHoraFin());
        existing.setPlannedHours(user.getPlannedHours());
        existing.setTurno(user.getTurno());
        existing.setHourlyRate(user.getHourlyRate());

        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existing.setPassword(user.getPassword());
        }

        return userRepository.save(existing);
    }

    public Optional<Users> getUserById(Integer id) {
        return userRepository.findById(id);
    }

    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }

    public List<Users> getActiveUsers() {
        return userRepository.findByState(true);
    }

    @Transactional
    public void deactivateUser(Integer id) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        user.setState(false);
        userRepository.save(user);
    }

    @Transactional
    public void activateUser(Integer id) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        user.setState(true);
        userRepository.save(user);
    }

    @Transactional
    public void physicalDeleteUser(Integer id) {
        userRepository.deleteById(id);
    }

    public Optional<Users> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<Users> getUserByDocumentNumber(String documentNumber) {
        return userRepository.findByDocumentNumber(documentNumber);
    }

    public List<Users> searchUsersByName(String search) {
        return userRepository.searchByNameOrSurnames(search);
    }

    public List<Users> getUsersByRole(Integer roleId) {
        return userRepository.findByRole_IdRole(roleId);
    }

    public List<Users> getUsersByArea(Integer areaId) {
        return userRepository.findByArea_IdArea(areaId);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByDocumentNumber(String documentNumber) {
        return userRepository.existsByDocumentNumber(documentNumber);
    }

    // =========================
    // FOTO PERFIL (MISMA LÓGICA QUE PRESENTACIONES)
    // =========================
    @Transactional
    public Users uploadProfilePhoto(Integer id, MultipartFile file) {

        Users user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("El archivo debe ser una imagen válida");
            }

            long maxSize = 5 * 1024 * 1024;
            if (file.getSize() > maxSize) {
                throw new IllegalArgumentException("El archivo no debe exceder 5MB");
            }

            String ext = file.getOriginalFilename()
                    .substring(file.getOriginalFilename().lastIndexOf("."));

            String fileName = "user_" + id + "_" + System.currentTimeMillis() + ext;

            Path path = Paths.get(UPLOAD_DIR, fileName);
            Files.write(path, file.getBytes());

            // eliminar foto anterior si existe
            if (user.getProfilePhoto() != null) {
                String oldFile = user.getProfilePhoto().replace("/usuarios/", "");
                Files.deleteIfExists(Paths.get(UPLOAD_DIR, oldFile));
            }

            // guardar RUTA RELATIVA (IGUAL QUE PRESENTACIONES)
            user.setProfilePhoto("/usuarios/" + fileName);

            return userRepository.save(user);

        } catch (Exception e) {
            throw new RuntimeException("Error guardando imagen", e);
        }
    }

    @Transactional
    public void deleteProfilePhoto(Integer id) {

        Users user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (user.getProfilePhoto() != null) {
            try {
                String fileName = user.getProfilePhoto().replace("/usuarios/", "");
                Files.deleteIfExists(Paths.get(UPLOAD_DIR, fileName));
            } catch (Exception ignored) {
            }
        }

        user.setProfilePhoto(null);
        userRepository.save(user);
    }
}
