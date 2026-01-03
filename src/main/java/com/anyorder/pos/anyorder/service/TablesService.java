package com.anyorder.pos.anyorder.service;

import com.anyorder.pos.anyorder.model.Tables;
import com.anyorder.pos.anyorder.repository.TablesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TablesService {

    private final TablesRepository tablesRepository;
    private final AreaService areaService;

    @Transactional(readOnly = true)
    public List<Tables> findAll() {
        log.debug("Obteniendo todas las mesas");
        return tablesRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Tables> findAllActive() {
        log.debug("Obteniendo mesas activas");
        return tablesRepository.findByStateTrue();
    }

    @Transactional(readOnly = true)
    public Tables findById(Integer id) {
        log.debug("Buscando mesa con ID: {}", id);
        return tablesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada con ID: " + id));
    }

    @Transactional
    public Tables create(Tables tables) {
        log.info("Creando nueva mesa: {}", tables.getName());

        // Validar nombre único
        if (tablesRepository.existsByName(tables.getName())) {
            throw new RuntimeException("Ya existe una mesa con el nombre: " + tables.getName());
        }

        // Validar nombre
        if (tables.getName() == null || tables.getName().trim().length() < 2) {
            throw new RuntimeException("El nombre de la mesa debe tener al menos 2 caracteres");
        }

        // Validar área existe
        if (tables.getArea() == null || tables.getArea().getIdArea() == null) {
            throw new RuntimeException("El área es obligatoria");
        }
        areaService.findById(tables.getArea().getIdArea());

        // Validar capacidad
        if (tables.getCapacity() == null || tables.getCapacity() < 1 || tables.getCapacity() > 50) {
            throw new RuntimeException("La capacidad debe estar entre 1 y 50 personas");
        }

        // Validar ubicación
        if (tables.getLocation() == null || tables.getLocation().trim().length() < 2) {
            throw new RuntimeException("La ubicación debe tener al menos 2 caracteres");
        }

        // Generar QR Token si no existe
        if (tables.getQrToken() == null || tables.getQrToken().isEmpty()) {
            tables.setQrToken(generateQrToken());
        }

        // Inicializar estado
        if (tables.getState() == null) {
            tables.setState(true);
        }
        if (tables.getIsOccupied() == null) {
            tables.setIsOccupied(false);
        }
        if (tables.getCurrentOccupancy() == null) {
            tables.setCurrentOccupancy(0);
        }

        Tables savedTables = tablesRepository.save(tables);
        log.info("Mesa creada exitosamente con ID: {}", savedTables.getIdTable());

        return savedTables;
    }

    @Transactional
    public Tables update(Integer id, Tables tablesData) {
        log.info("Actualizando mesa con ID: {}", id);

        Tables tables = findById(id);

        // Validar nombre único (si cambió)
        if (!tables.getName().equals(tablesData.getName())) {
            if (tablesRepository.existsByName(tablesData.getName())) {
                throw new RuntimeException("Ya existe una mesa con el nombre: " + tablesData.getName());
            }
        }

        // Validar área existe (si cambió)
        if (tablesData.getArea() != null && !tables.getArea().getIdArea().equals(tablesData.getArea().getIdArea())) {
            areaService.findById(tablesData.getArea().getIdArea());
        }

        // Validar capacidad
        if (tablesData.getCapacity() != null) {
            if (tablesData.getCapacity() < 1 || tablesData.getCapacity() > 50) {
                throw new RuntimeException("La capacidad debe estar entre 1 y 50 personas");
            }
            // Si la nueva capacidad es menor que la ocupación actual, rechazar
            if (tablesData.getCapacity() < tables.getCurrentOccupancy()) {
                throw new RuntimeException(
                        "No puede establecer capacidad menor a la ocupación actual (" + tables.getCurrentOccupancy()
                                + ")");
            }
            tables.setCapacity(tablesData.getCapacity());
        }

        // Actualizar campos
        tables.setName(tablesData.getName());
        if (tablesData.getArea() != null) {
            tables.setArea(tablesData.getArea());
        }
        tables.setLocation(tablesData.getLocation());

        if (tablesData.getQrCode() != null) {
            tables.setQrCode(tablesData.getQrCode());
        }

        if (tablesData.getState() != null) {
            tables.setState(tablesData.getState());
        }

        Tables updatedTables = tablesRepository.save(tables);
        log.info("Mesa actualizada exitosamente con ID: {}", updatedTables.getIdTable());

        return updatedTables;
    }

    @Transactional
    public void deactivate(Integer id) {
        log.info("Desactivando mesa con ID: {}", id);
        Tables tables = findById(id);
        tables.setState(false);
        tablesRepository.save(tables);
        log.info("Mesa {} desactivada exitosamente", id);
    }

    @Transactional
    public void activate(Integer id) {
        log.info("Activando mesa con ID: {}", id);
        Tables tables = findById(id);
        tables.setState(true);
        tablesRepository.save(tables);
        log.info("Mesa {} activada exitosamente", id);
    }

    @Transactional
    public void delete(Integer id) {
        log.info("Eliminando mesa con ID: {}", id);
        if (!tablesRepository.existsById(id)) {
            throw new RuntimeException("Mesa no encontrada con ID: " + id);
        }
        tablesRepository.deleteById(id);
        log.info("Mesa {} eliminada exitosamente", id);
    }

    @Transactional(readOnly = true)
    public Tables findByName(String name) {
        log.debug("Buscando mesa por nombre: {}", name);
        return tablesRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada con nombre: " + name));
    }

    @Transactional(readOnly = true)
    public Tables findByQrToken(String qrToken) {
        log.debug("Buscando mesa por QR token");
        return tablesRepository.findByQrToken(qrToken)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada con QR token: " + qrToken));
    }

    @Transactional(readOnly = true)
    public List<Tables> findByArea(Integer areaId) {
        log.debug("Obteniendo mesas del área: {}", areaId);
        areaService.findById(areaId);
        return tablesRepository.findByArea_IdArea(areaId);
    }

    @Transactional(readOnly = true)
    public List<Tables> findActiveByArea(Integer areaId) {
        log.debug("Obteniendo mesas activas del área: {}", areaId);
        areaService.findById(areaId);
        return tablesRepository.findActiveTablesByArea(areaId);
    }

    @Transactional(readOnly = true)
    public List<Tables> findAvailableTables() {
        log.debug("Obteniendo mesas disponibles");
        return tablesRepository.findAvailableTables();
    }

    @Transactional(readOnly = true)
    public List<Tables> findAvailableTablesByArea(Integer areaId) {
        log.debug("Obteniendo mesas disponibles del área: {}", areaId);
        areaService.findById(areaId);
        return tablesRepository.findAvailableTablesByArea(areaId);
    }

    @Transactional(readOnly = true)
    public List<Tables> findOccupiedTables() {
        log.debug("Obteniendo mesas ocupadas");
        return tablesRepository.findOccupiedTablesOrderByTime();
    }

    @Transactional(readOnly = true)
    public List<Tables> searchByName(String name) {
        log.debug("Buscando mesas por nombre: {}", name);
        return tablesRepository.findByNameContainingIgnoreCase(name);
    }

    @Transactional(readOnly = true)
    public List<Tables> searchByLocation(String location) {
        log.debug("Buscando mesas por ubicación: {}", location);
        return tablesRepository.findByLocationContainingIgnoreCase(location);
    }

    @Transactional
    public Tables occupyTable(Integer id, Integer numberOfPeople) {
        log.info("Ocupando mesa {} con {} personas", id, numberOfPeople);

        Tables tables = findById(id);

        // Validar estado
        if (!tables.getState()) {
            throw new RuntimeException("No se puede ocupar una mesa inactiva");
        }

        // Validar capacidad
        if (numberOfPeople > tables.getCapacity()) {
            throw new RuntimeException(
                    "El número de personas (" + numberOfPeople +
                            ") excede la capacidad de la mesa (" + tables.getCapacity() + ")");
        }

        if (numberOfPeople < 1) {
            throw new RuntimeException("Debe haber al menos 1 persona");
        }

        tables.setIsOccupied(true);
        tables.setCurrentOccupancy(numberOfPeople);
        tables.setOccupiedSince(LocalDateTime.now());

        Tables updatedTables = tablesRepository.save(tables);
        log.info("Mesa {} ocupada exitosamente", id);

        return updatedTables;
    }

    @Transactional
    public Tables releaseTable(Integer id) {
        log.info("Liberando mesa con ID: {}", id);

        Tables tables = findById(id);

        if (!tables.getIsOccupied()) {
            throw new RuntimeException("La mesa no está ocupada");
        }

        tables.setIsOccupied(false);
        tables.setCurrentOccupancy(0);
        tables.setOccupiedSince(null);

        Tables updatedTables = tablesRepository.save(tables);
        log.info("Mesa {} liberada exitosamente", id);

        return updatedTables;
    }

    @Transactional
    public Tables updateOccupancy(Integer id, Integer newOccupancy) {
        log.info("Actualizando ocupancia de mesa {} a {} personas", id, newOccupancy);

        Tables tables = findById(id);

        if (!tables.getIsOccupied()) {
            throw new RuntimeException("La mesa no está ocupada");
        }

        if (newOccupancy < 1) {
            throw new RuntimeException("Debe haber al menos 1 persona");
        }

        if (newOccupancy > tables.getCapacity()) {
            throw new RuntimeException(
                    "El número de personas (" + newOccupancy +
                            ") excede la capacidad de la mesa (" + tables.getCapacity() + ")");
        }

        tables.setCurrentOccupancy(newOccupancy);
        Tables updatedTables = tablesRepository.save(tables);
        log.info("Ocupancia de mesa {} actualizada a {}", id, newOccupancy);

        return updatedTables;
    }

    @Transactional(readOnly = true)
    public long countActive() {
        log.debug("Contando mesas activas");
        return tablesRepository.countByStateTrue();
    }

    @Transactional(readOnly = true)
    public long countOccupied() {
        log.debug("Contando mesas ocupadas");
        return tablesRepository.countOccupiedTables();
    }

    @Transactional(readOnly = true)
    public long countAvailable() {
        log.debug("Contando mesas disponibles");
        return tablesRepository.countAvailableTables();
    }

    @Transactional(readOnly = true)
    public long countByArea(Integer areaId) {
        log.debug("Contando mesas del área: {}", areaId);
        areaService.findById(areaId);
        return tablesRepository.countActiveTablesByArea(areaId);
    }

    /**
     * Genera un token único para QR
     */
    private String generateQrToken() {
        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        // Asegurar que sea único
        while (tablesRepository.existsByQrToken(token)) {
            token = UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        }
        return token;
    }
}