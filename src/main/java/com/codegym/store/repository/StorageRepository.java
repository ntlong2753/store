package com.codegym.store.repository;

import com.codegym.store.model.Storage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StorageRepository extends JpaRepository<Storage, Long> {
    List<Storage> findByNameContainingIgnoreCase(String keyword);
}
