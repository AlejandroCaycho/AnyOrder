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
}
