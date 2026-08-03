package com.codegym.store.repository;

import com.codegym.store.model.Casepc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CasepcRepository extends JpaRepository<Casepc, Long> {
    List<Casepc> findByNameContainingIgnoreCase(String keyword);
}
