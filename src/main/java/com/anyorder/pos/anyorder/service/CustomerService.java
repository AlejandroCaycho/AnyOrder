package com.anyorder.pos.anyorder.service;

import com.anyorder.pos.anyorder.model.Customer;
import com.anyorder.pos.anyorder.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional(readOnly = true)
    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Customer> findAllActive() {
        return customerRepository.findByStateTrue();
    }

    @Transactional(readOnly = true)
    public Customer findById(Integer id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));
    }

    @Transactional(readOnly = true)
    public Customer findByDocumentNumber(String documentNumber) {
        return customerRepository.findByDocumentNumber(documentNumber)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con documento: " + documentNumber));
    }

    @Transactional(readOnly = true)
    public Customer findByEmail(String email) {
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con email: " + email));
    }

    @Transactional
    public Customer create(Customer customer) {

        if (customer.getFirstName() == null || customer.getFirstName().trim().length() < 2) {
            throw new RuntimeException("El nombre debe tener al menos 2 caracteres");
        }

        if (customer.getLastName() == null || customer.getLastName().trim().length() < 2) {
            throw new RuntimeException("El apellido debe tener al menos 2 caracteres");
        }

        if (customer.getPhone() == null || customer.getPhone().trim().length() < 9) {
            throw new RuntimeException("El teléfono debe tener al menos 9 dígitos");
        }

        if (customerRepository.existsByDocumentNumber(customer.getDocumentNumber())) {
            throw new RuntimeException("Ya existe un cliente con el documento: " + customer.getDocumentNumber());
        }

        if (customer.getEmail() != null && !customer.getEmail().isEmpty()) {
            if (customerRepository.existsByEmail(customer.getEmail())) {
                throw new RuntimeException("Ya existe un cliente con el email: " + customer.getEmail());
            }
        }

        if (customer.getDocumentNumber() == null || customer.getDocumentNumber().trim().length() < 8) {
            throw new RuntimeException("El documento debe tener al menos 8 caracteres");
        }

        return customerRepository.save(customer);
    }

    @Transactional
    public Customer update(Integer id, Customer customerData) {
        Customer customer = findById(id);

        if (customerData.getFirstName() != null && !customerData.getFirstName().trim().isEmpty()) {
            if (customerData.getFirstName().trim().length() < 2) {
                throw new RuntimeException("El nombre debe tener al menos 2 caracteres");
            }
            customer.setFirstName(customerData.getFirstName());
        }

        if (customerData.getLastName() != null && !customerData.getLastName().trim().isEmpty()) {
            if (customerData.getLastName().trim().length() < 2) {
                throw new RuntimeException("El apellido debe tener al menos 2 caracteres");
            }
            customer.setLastName(customerData.getLastName());
        }

        if (customerData.getPhone() != null && !customerData.getPhone().trim().isEmpty()) {
            if (customerData.getPhone().trim().length() < 9) {
                throw new RuntimeException("El teléfono debe tener al menos 9 dígitos");
            }
            customer.setPhone(customerData.getPhone());
        }

        if (customerData.getEmail() != null && !customerData.getEmail().isEmpty()) {
            if (!customer.getEmail().equals(customerData.getEmail()) &&
                    customerRepository.existsByEmail(customerData.getEmail())) {
                throw new RuntimeException("Ya existe un cliente con el email: " + customerData.getEmail());
            }
            customer.setEmail(customerData.getEmail());
        }

        if (customerData.getAddress() != null) {
            customer.setAddress(customerData.getAddress());
        }

        if (customerData.getCustomerType() != null) {
            customer.setCustomerType(customerData.getCustomerType());
        }

        if (customerData.getRole() != null) {
            customer.setRole(customerData.getRole());
        }

        if (customerData.getState() != null) {
            customer.setState(customerData.getState());
        }

        return customerRepository.save(customer);
    }

    @Transactional
    public void deactivate(Integer id) {
        Customer customer = findById(id);
        customer.setState(false);
        customerRepository.save(customer);
    }

    @Transactional
    public void activate(Integer id) {
        Customer customer = findById(id);
        customer.setState(true);
        customerRepository.save(customer);
    }

    @Transactional
    public void delete(Integer id) {
        if (!customerRepository.existsById(id)) {
            throw new RuntimeException("Cliente no encontrado con ID: " + id);
        }
        customerRepository.deleteById(id);
    }

    @Transactional
    public void updatePurchaseInfo(Integer customerId, BigDecimal amount) {
        Customer customer = findById(customerId);
        customer.setTotalPurchases(customer.getTotalPurchases() + 1);
        customer.setTotalSpent(customer.getTotalSpent().add(amount));
        customer.setLastPurchaseDate(LocalDateTime.now());
        customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public List<Customer> searchByName(String name) {
        return customerRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(name, name);
    }

    @Transactional(readOnly = true)
    public List<Customer> findByCustomerType(Customer.CustomerType customerType) {
        return customerRepository.findByCustomerType(customerType);
    }

    @Transactional(readOnly = true)
    public List<Customer> findByDocumentType(Customer.DocumentType documentType) {
        return customerRepository.findByDocumentType(documentType);
    }

    @Transactional(readOnly = true)
    public List<Customer> findVIPCustomers(Integer minPurchases) {
        return customerRepository.findVIPCustomers(minPurchases);
    }

    @Transactional(readOnly = true)
    public List<Customer> findRecentCustomers(Integer limit) {
        return customerRepository.findRecentCustomers(limit);
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return customerRepository.countByStateTrue();
    }

    @Transactional(readOnly = true)
    public long countByCustomerType(Customer.CustomerType customerType) {
        return customerRepository.countByCustomerType(customerType);
    }
}