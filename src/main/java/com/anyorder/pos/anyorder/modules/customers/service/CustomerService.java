package com.anyorder.pos.anyorder.modules.customers.service;

import com.anyorder.pos.anyorder.modules.customers.model.Customer;
import com.anyorder.pos.anyorder.modules.customers.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    public List<Customer> findAllActive() {
        return customerRepository.findByStateTrue();
    }

    public Optional<Customer> findById(Integer id) {
        return customerRepository.findById(id);
    }

    public Optional<Customer> findByDocumentNumber(String documentNumber) {
        return customerRepository.findByDocumentNumber(documentNumber);
    }

    @Transactional
    public Customer create(Customer customer) {
        if (customerRepository.existsByDocumentNumber(customer.getDocumentNumber())) {
            throw new IllegalArgumentException("Ya existe un cliente con el documento: " + customer.getDocumentNumber());
        }
        return customerRepository.save(customer);
    }

    @Transactional
    public Customer update(Integer id, Customer customerDetails) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));

        if (!customer.getDocumentNumber().equals(customerDetails.getDocumentNumber()) &&
                customerRepository.existsByDocumentNumber(customerDetails.getDocumentNumber())) {
            throw new IllegalArgumentException("Ya existe un cliente con ese número de documento");
        }

        customer.setFirstName(customerDetails.getFirstName());
        customer.setLastName(customerDetails.getLastName());
        customer.setEmail(customerDetails.getEmail());
        customer.setPhone(customerDetails.getPhone());
        customer.setDocumentType(customerDetails.getDocumentType());
        customer.setDocumentNumber(customerDetails.getDocumentNumber());
        customer.setAddress(customerDetails.getAddress());
        customer.setCustomerType(customerDetails.getCustomerType());
        customer.setRole(customerDetails.getRole());
        customer.setState(customerDetails.getState());

        return customerRepository.save(customer);
    }

    @Transactional
    public void delete(Integer id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));
        customer.setState(false);
        customerRepository.save(customer);
    }

    @Transactional
    public void deletePermanently(Integer id) {
        if (!customerRepository.existsById(id)) {
            throw new RuntimeException("Cliente no encontrado con ID: " + id);
        }
        customerRepository.deleteById(id);
    }

    @Transactional
    public Customer activate(Integer id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));
        customer.setState(true);
        return customerRepository.save(customer);
    }

    @Transactional
    public void updatePurchaseInfo(Integer customerId, BigDecimal amount) {
        customerRepository.findById(customerId).ifPresent(customer -> {
            customer.setTotalPurchases(customer.getTotalPurchases() + 1);
            customer.setTotalSpent(customer.getTotalSpent().add(amount));
            customer.setLastPurchaseDate(LocalDateTime.now());
            customerRepository.save(customer);
        });
    }
}
