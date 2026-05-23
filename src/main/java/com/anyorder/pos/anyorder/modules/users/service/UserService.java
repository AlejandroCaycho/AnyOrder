package com.anyorder.pos.anyorder.modules.users.service;

import com.anyorder.pos.anyorder.modules.users.model.User;
import com.anyorder.pos.anyorder.modules.users.repository.UserRepository;
import com.anyorder.pos.anyorder.modules.roles.repository.RoleRepository;
import com.anyorder.pos.anyorder.modules.areas.repository.AreaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AreaRepository areaRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public List<User> findAllActive() {
        return userRepository.findByStateTrue();
    }

    public Optional<User> findById(Integer id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByDocumentNumber(String documentNumber) {
        return userRepository.findByDocumentNumber(documentNumber);
    }

    public List<User> findByRole(Integer idRole) {
        return userRepository.findByRole_IdRole(idRole);
    }

    public List<User> findByArea(Integer idArea) {
        return userRepository.findByArea_IdArea(idArea);
    }

    public List<User> findByDateRange(LocalDateTime start, LocalDateTime end) {
        return userRepository.findByDateRange(start, end);
    }

    @Transactional
    public User create(User user) {
        validateUserUniqueness(user);
        
        // Validate Role and Area
        if (!roleRepository.existsById(user.getRole().getIdRole())) {
            throw new RuntimeException("Rol no encontrado");
        }
        if (!areaRepository.existsById(user.getArea().getIdArea())) {
            throw new RuntimeException("Área no encontrada");
        }

        // Hash password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setState(true);
        
        log.info("Creando usuario: {} {}", user.getName(), user.getSurnames());
        return userRepository.save(user);
    }

    @Transactional
    public User update(Integer id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        validateUserUniquenessForUpdate(user, userDetails);

        user.setName(userDetails.getName());
        user.setSurnames(userDetails.getSurnames());
        user.setEmail(userDetails.getEmail());
        user.setDocumentNumber(userDetails.getDocumentNumber());
        user.setDocumentType(userDetails.getDocumentType());
        user.setGender(userDetails.getGender());
        user.setPhone(userDetails.getPhone());
        user.setAdress(userDetails.getAdress());
        user.setProfilePhoto(userDetails.getProfilePhoto());
        user.setRole(userDetails.getRole());
        user.setArea(userDetails.getArea());
        user.setHoraInicio(userDetails.getHoraInicio());
        user.setHoraFin(userDetails.getHoraFin());
        user.setPlannedHours(userDetails.getPlannedHours());
        user.setTurno(userDetails.getTurno());
        user.setHourlyRate(userDetails.getHourlyRate());
        user.setState(userDetails.getState());

        if (userDetails.getPassword() != null && !userDetails.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        return userRepository.save(user);
    }

    @Transactional
    public void delete(Integer id) {
        User user = findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        user.setState(false);
        userRepository.save(user);
    }

    @Transactional
    public User activate(Integer id) {
        User user = findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        user.setState(true);
        return userRepository.save(user);
    }

    public Optional<User> login(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()));
    }

    public List<Map<String, Object>> getRoleStats() {
        List<User> allUsers = userRepository.findAll();
        return allUsers.stream()
                .collect(Collectors.groupingBy(u -> u.getRole().getNameRole()))
                .entrySet()
                .stream()
                .map(entry -> {
                    String roleName = entry.getKey();
                    List<User> usersByRole = entry.getValue();
                    long total = usersByRole.size();
                    long activos = usersByRole.stream().filter(u -> Boolean.TRUE.equals(u.getState())).count();
                    
                    Map<String, Object> stat = new HashMap<>();
                    stat.put("role", roleName);
                    stat.put("total", total);
                    stat.put("active", activos);
                    stat.put("inactive", total - activos);
                    return stat;
                })
                .collect(Collectors.toList());
    }

    private void validateUserUniqueness(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }
        if (userRepository.existsByDocumentNumber(user.getDocumentNumber())) {
            throw new IllegalArgumentException("El número de documento ya está registrado");
        }
    }

    private void validateUserUniquenessForUpdate(User existingUser, User userDetails) {
        if (!existingUser.getEmail().equalsIgnoreCase(userDetails.getEmail()) &&
                userRepository.existsByEmail(userDetails.getEmail())) {
            throw new IllegalArgumentException("El nuevo email ya está registrado");
        }
        if (!existingUser.getDocumentNumber().equals(userDetails.getDocumentNumber()) &&
                userRepository.existsByDocumentNumber(userDetails.getDocumentNumber())) {
            throw new IllegalArgumentException("El nuevo número de documento ya está registrado");
        }
    }

    @Transactional
    public User uploadPhoto(Integer id, MultipartFile file) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede estar vacío");
        }

        try {
            // Get original extension
            String originalFilename = file.getOriginalFilename();
            String extension = "png"; // default
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            }

            // Generate clean filename: user_{idUser}_{timestamp}.{extension}
            String filename = "user_" + id + "_" + System.currentTimeMillis() + "." + extension;

            // Paths to write - robust detection of multi-module/parent working directory
            String projectPath = System.getProperty("user.dir");
            File baseDir = new File(projectPath);
            if (new File(baseDir, "AnyOrder-be").exists()) {
                baseDir = new File(baseDir, "AnyOrder-be");
            }
            File srcDir = new File(baseDir, "src/main/resources/static/usuarios");
            File targetDir = new File(baseDir, "target/classes/static/usuarios");

            // Ensure directories exist
            if (!srcDir.exists()) srcDir.mkdirs();
            if (!targetDir.exists()) targetDir.mkdirs();

            // Delete old photo if it exists
            if (user.getProfilePhoto() != null) {
                String oldPhotoPath = user.getProfilePhoto();
                if (oldPhotoPath.startsWith("/usuarios/")) {
                    String oldFilename = oldPhotoPath.substring("/usuarios/".length());
                    new File(srcDir, oldFilename).delete();
                    new File(targetDir, oldFilename).delete();
                }
            }

            // Copy file to src and target
            File srcFile = new File(srcDir, filename);
            File targetFile = new File(targetDir, filename);

            // Copy once from input stream to physical src folder
            Files.copy(file.getInputStream(), srcFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            
            // Replicate from src file to target compiled folder to avoid stream consumption issues
            try {
                Files.copy(srcFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                // Ignore target copy failures if classes folder doesn't exist yet
            }

            // Set user profile photo path (relative URL)
            user.setProfilePhoto("/usuarios/" + filename);
            return userRepository.save(user);

        } catch (Exception e) {
            throw new RuntimeException("Error al guardar la foto de perfil: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void deletePhoto(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (user.getProfilePhoto() != null) {
            try {
                String photoPath = user.getProfilePhoto();
                if (photoPath.startsWith("/usuarios/")) {
                    String filename = photoPath.substring("/usuarios/".length());
                    String projectPath = System.getProperty("user.dir");
                    File baseDir = new File(projectPath);
                    if (new File(baseDir, "AnyOrder-be").exists()) {
                        baseDir = new File(baseDir, "AnyOrder-be");
                    }
                    new File(new File(baseDir, "src/main/resources/static/usuarios"), filename).delete();
                    new File(new File(baseDir, "target/classes/static/usuarios"), filename).delete();
                }
            } catch (Exception e) {
                log.error("Error al eliminar archivo físico de foto de perfil", e);
            }
            user.setProfilePhoto(null);
            userRepository.save(user);
        }
    }
}
