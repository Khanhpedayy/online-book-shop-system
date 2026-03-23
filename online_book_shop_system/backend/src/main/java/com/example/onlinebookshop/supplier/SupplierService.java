package com.example.onlinebookshop.supplier;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SupplierService {

    private final SupplierRepository repo;

    public SupplierService(SupplierRepository repo) {
        this.repo = repo;
    }

    public List<SupplierDTO> getAll() {
        return repo.findAll();
    }

    public SupplierDTO getById(Long id) {
        SupplierDTO s = repo.findById(id);
        if (s == null)
            throw new RuntimeException("Supplier not found: " + id);
        return s;
    }

    @Transactional
    public SupplierDTO create(CreateSupplierRequest req) {
        if (req.getName() == null || req.getName().isBlank())
            throw new IllegalArgumentException("Supplier name is required");
        if (req.getCode() == null || req.getCode().isBlank())
            throw new IllegalArgumentException("Supplier code is required");
        Long id = repo.insert(req);
        return repo.findById(id);
    }

    @Transactional
    public SupplierDTO update(Long id, UpdateSupplierRequest req) {
        if (req.getName() != null && req.getName().isBlank())
            throw new IllegalArgumentException("Supplier name cannot be empty");
        int rows = repo.update(id, req);
        if (rows == 0)
            throw new RuntimeException("Supplier not found: " + id);
        return repo.findById(id);
    }

    @Transactional
    public void delete(Long id) {
        int rows = repo.softDelete(id);
        if (rows == 0)
            throw new RuntimeException("Supplier not found: " + id);
    }
}

