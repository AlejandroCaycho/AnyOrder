package com.anyorder.pos.anyorder.service;

import com.anyorder.pos.anyorder.model.Supplier;
import com.anyorder.pos.anyorder.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";

    @Transactional(readOnly = true)
    public List<Supplier> findAll() {
        return supplierRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Supplier> findAllActive() {
        return supplierRepository.findByStateTrue();
    }

    @Transactional(readOnly = true)
    public Supplier findById(Integer id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con ID: " + id));
    }

    @Transactional(readOnly = true)
    public Supplier findByDocumentNumber(String documentNumber) {
        return supplierRepository.findByDocumentNumber(documentNumber)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con documento: " + documentNumber));
    }

    @Transactional
    public Supplier create(Supplier supplier) {
        validateSupplier(supplier);
        return supplierRepository.save(supplier);
    }

    @Transactional
    public Supplier update(Integer id, Supplier supplierData) {
        Supplier supplier = findById(id);

        if (!supplier.getDocumentNumber().equals(supplierData.getDocumentNumber()) &&
                supplierRepository.existsByDocumentNumber(supplierData.getDocumentNumber())) {
            throw new RuntimeException("Ya existe un proveedor con el documento: " + supplierData.getDocumentNumber());
        }

        if (supplierData.getName() != null && supplierData.getName().trim().length() >= 3) {
            supplier.setName(supplierData.getName());
        }

        if (supplierData.getContactName() != null && supplierData.getContactName().trim().length() >= 3) {
            supplier.setContactName(supplierData.getContactName());
        }

        if (supplierData.getPhone() != null && supplierData.getPhone().trim().length() >= 9) {
            supplier.setPhone(supplierData.getPhone());
        }

        if (supplierData.getEmail() != null && !supplierData.getEmail().isEmpty()) {
            if (isValidEmail(supplierData.getEmail())) {
                supplier.setEmail(supplierData.getEmail());
            } else {
                throw new RuntimeException("El email no tiene un formato válido");
            }
        }

        if (supplierData.getAddress() != null && supplierData.getAddress().trim().length() >= 5) {
            supplier.setAddress(supplierData.getAddress());
        }

        if (supplierData.getDocumentType() != null) {
            supplier.setDocumentType(supplierData.getDocumentType());
        }

        if (supplierData.getDocumentNumber() != null) {
            supplier.setDocumentNumber(supplierData.getDocumentNumber());
        }

        if (supplierData.getPaymentTerms() != null) {
            supplier.setPaymentTerms(supplierData.getPaymentTerms());
        }

        if (supplierData.getNotes() != null) {
            supplier.setNotes(supplierData.getNotes());
        }

        if (supplierData.getState() != null) {
            supplier.setState(supplierData.getState());
        }

        return supplierRepository.save(supplier);
    }

    @Transactional
    public void deactivate(Integer id) {
        Supplier supplier = findById(id);
        supplier.setState(false);
        supplierRepository.save(supplier);
    }

    @Transactional
    public void activate(Integer id) {
        Supplier supplier = findById(id);
        supplier.setState(true);
        supplierRepository.save(supplier);
    }

    @Transactional
    public void delete(Integer id) {
        if (!supplierRepository.existsById(id)) {
            throw new RuntimeException("Proveedor no encontrado con ID: " + id);
        }
        supplierRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Supplier> searchByName(String name) {
        return supplierRepository.findByNameContainingIgnoreCase(name);
    }

    @Transactional(readOnly = true)
    public List<Supplier> findByDocumentType(Supplier.DocumentType documentType) {
        return supplierRepository.findByDocumentType(documentType);
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return supplierRepository.countByStateTrue();
    }

    private void validateSupplier(Supplier supplier) {
        if (supplier.getName() == null || supplier.getName().trim().length() < 3) {
            throw new RuntimeException("El nombre debe tener al menos 3 caracteres");
        }

        if (supplier.getContactName() == null || supplier.getContactName().trim().length() < 3) {
            throw new RuntimeException("El contacto debe tener al menos 3 caracteres");
        }

        if (supplier.getPhone() == null || supplier.getPhone().trim().length() < 9) {
            throw new RuntimeException("El teléfono debe tener al menos 9 dígitos");
        }

        if (supplier.getEmail() != null && !supplier.getEmail().isEmpty()) {
            if (!isValidEmail(supplier.getEmail())) {
                throw new RuntimeException("El email no tiene un formato válido");
            }
        }

        if (supplier.getAddress() == null || supplier.getAddress().trim().length() < 5) {
            throw new RuntimeException("La dirección debe tener al menos 5 caracteres");
        }

        if (supplier.getDocumentNumber() == null || supplier.getDocumentNumber().trim().length() < 8) {
            throw new RuntimeException("El documento debe tener al menos 8 caracteres");
        }

        if (supplierRepository.existsByDocumentNumber(supplier.getDocumentNumber())) {
            throw new RuntimeException("Ya existe un proveedor con el documento: " + supplier.getDocumentNumber());
        }
    }

    private boolean isValidEmail(String email) {
        return Pattern.compile(EMAIL_REGEX).matcher(email).matches();
    }
}