package com.anyorder.pos.anyorder.service;

import com.anyorder.pos.anyorder.model.Area;
import com.anyorder.pos.anyorder.repository.AreaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AreaService {

    private final AreaRepository areaRepository;

    @Transactional(readOnly = true)
    public List<Area> findAll() {
        return areaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Area> findAllActive() {
        return areaRepository.findByStateTrue();
    }

    @Transactional(readOnly = true)
    public Area findById(Integer id) {
        return areaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Área no encontrada con ID: " + id));
    }

    @Transactional
    public Area create(Area area) {

        if (areaRepository.existsByNameArea(area.getNameArea())) {
            throw new RuntimeException("Ya existe un área con el nombre: " + area.getNameArea());
        }
        if (area.getNameArea() == null || area.getNameArea().trim().length() < 2) {
            throw new RuntimeException("El nombre del área debe tener al menos 2 caracteres");
        }

        return areaRepository.save(area);
    }

    @Transactional
    public Area update(Integer id, Area areaData) {
        Area area = findById(id);

        if (!area.getNameArea().equals(areaData.getNameArea()) &&
                areaRepository.existsByNameArea(areaData.getNameArea())) {
            throw new RuntimeException("Ya existe un área con el nombre: " + areaData.getNameArea());
        }

        area.setNameArea(areaData.getNameArea());
        area.setDescription(areaData.getDescription());

        if (areaData.getState() != null) {
            area.setState(areaData.getState());
        }

        return areaRepository.save(area);
    }

    @Transactional
    public void deactivate(Integer id) {
        Area area = findById(id);
        area.setState(false);
        areaRepository.save(area);
    }

    @Transactional
    public void activate(Integer id) {
        Area area = findById(id);
        area.setState(true);
        areaRepository.save(area);
    }

    @Transactional
    public void delete(Integer id) {
        if (!areaRepository.existsById(id)) {
            throw new RuntimeException("Área no encontrada con ID: " + id);
        }
        areaRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Area findByName(String name) {
        return areaRepository.findByNameArea(name)
                .orElseThrow(() -> new RuntimeException("Área no encontrada con nombre: " + name));
    }

    @Transactional(readOnly = true)
    public List<Area> searchByName(String name) {
        return areaRepository.findByNameAreaContainingIgnoreCase(name);
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return areaRepository.countByStateTrue();
    }
}