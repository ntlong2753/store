package com.codegym.store.repository;

import com.codegym.store.model.Ram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RamRepository extends JpaRepository<Ram, Long> {
    List<Ram> findByNameContainingIgnoreCase(String keyword);
}
