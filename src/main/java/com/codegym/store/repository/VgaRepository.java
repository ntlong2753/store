package com.codegym.store.repository;

import com.codegym.store.model.Vga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VgaRepository extends JpaRepository<Vga, Long> {
    List<Vga> findByNameContainingIgnoreCase(String keyword);
}
