package com.codegym.store.repository;

import com.codegym.store.model.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {
    // Lấy danh sách địa chỉ của 1 user, sắp xếp mới nhất lên đầu
    List<UserAddress> findByUserIdOrderByIdDesc(Long userId);

    List<UserAddress> findByUserId(Long userId);
}
