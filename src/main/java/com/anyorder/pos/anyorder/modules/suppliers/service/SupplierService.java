package com.anyorder.pos.anyorder.modules.suppliers.service;

import com.anyorder.pos.anyorder.modules.suppliers.model.Supplier;
import com.anyorder.pos.anyorder.modules.suppliers.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SupplierService {

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private com.anyorder.pos.anyorder.modules.sunat.service.SunatService sunatService;

    public List<Supplier> findAll() {
        return supplierRepository.findAll();
    }

    public List<Supplier> findAllActive() {
        return supplierRepository.findByStateTrue();
    }

    public Optional<Supplier> findById(Integer id) {
        return supplierRepository.findById(id);
    }

    public com.anyorder.pos.anyorder.modules.sunat.model.SunatData lookupRuc(String ruc) {
        return sunatService.consultarRuc(ruc);
    }

    @Transactional
    public Supplier create(Supplier supplier) {
        if (supplierRepository.existsByDocumentNumber(supplier.getDocumentNumber())) {
            throw new IllegalArgumentException("Ya existe un proveedor con ese número de documento");
        }
        return supplierRepository.save(supplier);
    }

    @Transactional
    public Supplier update(Integer id, Supplier supplierDetails) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con ID: " + id));

        if (!supplier.getDocumentNumber().equals(supplierDetails.getDocumentNumber()) &&
                supplierRepository.existsByDocumentNumber(supplierDetails.getDocumentNumber())) {
            throw new IllegalArgumentException("Ya existe un proveedor con ese número de documento");
        }

        supplier.setName(supplierDetails.getName());
        supplier.setContactName(supplierDetails.getContactName());
        supplier.setPhone(supplierDetails.getPhone());
        supplier.setEmail(supplierDetails.getEmail());
        supplier.setAddress(supplierDetails.getAddress());
        supplier.setDocumentType(supplierDetails.getDocumentType());
        supplier.setDocumentNumber(supplierDetails.getDocumentNumber());
        supplier.setPaymentTerms(supplierDetails.getPaymentTerms());
        supplier.setNotes(supplierDetails.getNotes());
        supplier.setState(supplierDetails.getState());

        return supplierRepository.save(supplier);
    }

    @Transactional
    public void delete(Integer id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con ID: " + id));
        supplier.setState(false);
        supplierRepository.save(supplier);
    }

    @Transactional
    public void deletePermanently(Integer id) {
        if (!supplierRepository.existsById(id)) {
            throw new RuntimeException("Proveedor no encontrado con ID: " + id);
        }
        supplierRepository.deleteById(id);
    }

    @Transactional
    public Supplier activate(Integer id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con ID: " + id));
        supplier.setState(true);
        return supplierRepository.save(supplier);
    }
}
