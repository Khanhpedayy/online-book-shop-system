package com.example.onlinebookshop.incident;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class IncidentService {
    private final IncidentRepository repo;

    public IncidentService(IncidentRepository repo) {
        this.repo = repo;
    }

    public List<IncidentDTO> getAll() {
        return repo.findAll();
    }

    @Transactional
    public void create(CreateIncidentRequest req) {
        repo.create(req);
    }
}

