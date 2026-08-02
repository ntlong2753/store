package com.codegym.store.repository;

import com.codegym.store.model.Cpu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CpuRepository extends JpaRepository<Cpu, Long> {
    // Spring sẽ tự biết lấy dữ liệu từ bảng `cpu` (và bảng cha `product`)
}
