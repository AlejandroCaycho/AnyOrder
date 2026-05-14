package com.anyorder.pos.anyorder.modules.areas.service;

import com.anyorder.pos.anyorder.modules.areas.model.Area;
import com.anyorder.pos.anyorder.modules.areas.repository.AreaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AreaService {

    private final AreaRepository areaRepository;

    public List<Area> findAll() {
        return areaRepository.findAll();
    }

    public List<Area> findAllActive() {
        return areaRepository.findByStateTrue();
    }

    public Optional<Area> findById(Integer id) {
        return areaRepository.findById(id);
    }

    public long countActive() {
        return areaRepository.countByStateTrue();
    }

    @Transactional
    public Area create(Area area) {
        if (areaRepository.existsByNameArea(area.getNameArea())) {
            throw new IllegalArgumentException("Ya existe un área con el nombre: " + area.getNameArea());
        }
        log.info("Creando nueva área: {}", area.getNameArea());
        return areaRepository.save(area);
    }

    @Transactional
    public Area update(Integer id, Area areaDetails) {
        Area area = areaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Área no encontrada con ID: " + id));

        if (!area.getNameArea().equalsIgnoreCase(areaDetails.getNameArea()) &&
                areaRepository.existsByNameArea(areaDetails.getNameArea())) {
            throw new IllegalArgumentException("Ya existe otra área con el nombre: " + areaDetails.getNameArea());
        }

        area.setNameArea(areaDetails.getNameArea());
        area.setDescription(areaDetails.getDescription());
        area.setState(areaDetails.getState());

        log.info("Actualizando área ID {}: {}", id, area.getNameArea());
        return areaRepository.save(area);
    }

    @Transactional
    public void delete(Integer id) {
        Area area = areaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Área no encontrada con ID: " + id));
        
        // Logical delete validation
        validateAreaUsage(id);
        
        area.setState(false);
        areaRepository.save(area);
        log.info("Área ID {} desactivada (borrado lógico)", id);
    }

    @Transactional
    public void hardDelete(Integer id) {
        if (!areaRepository.existsById(id)) {
            throw new RuntimeException("Área no encontrada con ID: " + id);
        }
        validateAreaUsage(id);
        areaRepository.deleteById(id);
        log.info("Área ID {} eliminada permanentemente", id);
    }

    @Transactional
    public Area activate(Integer id) {
        Area area = areaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Área no encontrada con ID: " + id));
        area.setState(true);
        return areaRepository.save(area);
    }

    public List<Area> searchByName(String name) {
        return areaRepository.findByNameAreaContainingIgnoreCase(name);
    }

    public List<Map<String, Object>> getAreaStats() {
        return areaRepository.findAll().stream().map(area -> {
            Map<String, Object> stat = new HashMap<>();
            stat.put("idArea", area.getIdArea());
            stat.put("nameArea", area.getNameArea());
            stat.put("userCount", areaRepository.countUsersByArea(area.getIdArea()));
            stat.put("productCount", areaRepository.countProductsByArea(area.getIdArea()));
            stat.put("tableCount", areaRepository.countTablesByArea(area.getIdArea()));
            stat.put("state", area.getState());
            return stat;
        }).collect(Collectors.toList());
    }

    private void validateAreaUsage(Integer idArea) {
        long userCount = areaRepository.countUsersByArea(idArea);
        if (userCount > 0) {
            throw new IllegalStateException("No se puede eliminar el área porque tiene " + userCount + " usuarios asociados");
        }
        
        long productCount = areaRepository.countProductsByArea(idArea);
        if (productCount > 0) {
            throw new IllegalStateException("No se puede eliminar el área porque tiene " + productCount + " productos asociados");
        }

        long tableCount = areaRepository.countTablesByArea(idArea);
        if (tableCount > 0) {
            throw new IllegalStateException("No se puede eliminar el área porque tiene " + tableCount + " mesas asociadas");
        }
    }
}
