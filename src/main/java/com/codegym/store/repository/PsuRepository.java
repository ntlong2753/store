package com.codegym.store.repository;

import com.codegym.store.model.Psu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PsuRepository extends JpaRepository<Psu, Long> {
    List<Psu> findByNameContainingIgnoreCase(String keyword);
}
