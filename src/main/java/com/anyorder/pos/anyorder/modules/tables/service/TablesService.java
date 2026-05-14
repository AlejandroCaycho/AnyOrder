package com.anyorder.pos.anyorder.modules.tables.service;

import com.anyorder.pos.anyorder.modules.tables.model.Tables;
import com.anyorder.pos.anyorder.modules.tables.repository.TablesRepository;
import com.anyorder.pos.anyorder.modules.areas.repository.AreaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TablesService {

    @Autowired
    private TablesRepository tablesRepository;

    @Autowired
    private AreaRepository areaRepository;

    public List<Tables> findAll() {
        return tablesRepository.findAll();
    }

    public List<Tables> findAllActive() {
        return tablesRepository.findByStateTrue();
    }

    public Optional<Tables> findById(Integer id) {
        return tablesRepository.findById(id);
    }

    public List<Tables> findByArea(Integer idArea) {
        return tablesRepository.findByArea_IdArea(idArea);
    }

    @Transactional
    public Tables create(Tables table) {
        if (tablesRepository.existsByName(table.getName())) {
            throw new IllegalArgumentException("Ya existe una mesa con ese nombre");
        }
        if (!areaRepository.existsById(table.getIdArea())) {
            throw new IllegalArgumentException("El área especificada no existe");
        }
        return tablesRepository.save(table);
    }

    @Transactional
    public Tables update(Integer id, Tables tableDetails) {
        Tables table = tablesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada con ID: " + id));

        if (!table.getName().equals(tableDetails.getName()) &&
                tablesRepository.existsByName(tableDetails.getName())) {
            throw new IllegalArgumentException("Ya existe una mesa con ese nombre");
        }

        if (!areaRepository.existsById(tableDetails.getIdArea())) {
            throw new IllegalArgumentException("El área especificada no existe");
        }

        table.setName(tableDetails.getName());
        table.setArea(areaRepository.findById(tableDetails.getIdArea()).get());
        table.setCapacity(tableDetails.getCapacity());
        table.setLocation(tableDetails.getLocation());
        table.setState(tableDetails.getState());

        return tablesRepository.save(table);
    }

    @Transactional
    public void delete(Integer id) {
        Tables table = tablesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada con ID: " + id));
        table.setState(false);
        tablesRepository.save(table);
    }

    @Transactional
    public Tables activate(Integer id) {
        Tables table = tablesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada con ID: " + id));
        table.setState(true);
        return tablesRepository.save(table);
    }

    @Transactional
    public Tables updateOccupancy(Integer id, Boolean isOccupied, Integer currentOccupancy) {
        Tables table = tablesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada con ID: " + id));

        table.setIsOccupied(isOccupied);
        table.setCurrentOccupancy(currentOccupancy);
        
        if (isOccupied) {
            table.setOccupiedSince(LocalDateTime.now());
        } else {
            table.setOccupiedSince(null);
            table.setCurrentOccupancy(0);
        }

        return tablesRepository.save(table);
    }

    /**
     * Decrementa la ocupación de la mesa cuando un pedido local se cierra.
     * Si la ocupación llega a 0, marca la mesa como libre.
     */
    @Transactional
    public void decrementOccupancy(Integer tableId, Integer numberOfPeople) {
        tablesRepository.findById(tableId).ifPresent(table -> {
            int newOccupancy = Math.max(0, table.getCurrentOccupancy() - numberOfPeople);
            table.setCurrentOccupancy(newOccupancy);
            if (newOccupancy == 0) {
                table.setIsOccupied(false);
                table.setOccupiedSince(null);
            }
            tablesRepository.save(table);
        });
    }
}
