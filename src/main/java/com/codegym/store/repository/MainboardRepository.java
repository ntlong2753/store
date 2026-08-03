package com.codegym.store.repository;

import com.codegym.store.model.Mainboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MainboardRepository extends JpaRepository<Mainboard, Long> {
    List<Mainboard> findByNameContainingIgnoreCase(String keyword);
}