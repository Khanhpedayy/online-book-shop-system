package com.example.onlinebookshop.returnsintake;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ReturnsIntakeService {
    private final ReturnsIntakeRepository repo;

    public ReturnsIntakeService(ReturnsIntakeRepository repo) {
        this.repo = repo;
    }

    public List<ReturnIntakeDTO> getAll() {
        return repo.findAll();
    }

    @Transactional
    public Long createIntake(CreateReturnIntakeRequest req) {
        return repo.createReturn(req);
    }

    @Transactional
    public void scanCopy(Long returnId, ScanReturnCopyRequest req) {
        repo.scanReturnCopy(returnId, req);
    }

    @Transactional
    public void recordCondition(Long itemId, RecordConditionRequest req) {
        int u = repo.recordCondition(itemId, req);
        if (u == 0)
            throw new RuntimeException("Return item not found: " + itemId);
    }

    @Transactional
    public void escalate(Long returnId) {
        int u = repo.escalateToManager(returnId);
        if (u == 0)
            throw new RuntimeException("Return not found: " + returnId);
    }
}

