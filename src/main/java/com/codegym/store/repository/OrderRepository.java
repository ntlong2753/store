package com.codegym.store.repository;

import com.codegym.store.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);
    List<Order> findByUserIdAndStatusInOrderByOrderDateDesc(Long userId, List<com.codegym.store.model.OrderStatus> statuses);


    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.status = 'DELIVERED' AND MONTH(o.orderDate) = MONTH(CURRENT_DATE) AND YEAR(o.orderDate) = YEAR(CURRENT_DATE)")
    java.math.BigDecimal sumRevenueThisMonth();

    @Query("SELECT COUNT(o) FROM Order o WHERE MONTH(o.orderDate) = MONTH(CURRENT_DATE) AND YEAR(o.orderDate) = YEAR(CURRENT_DATE)")
    Long countOrdersThisMonth();

    List<Order> findTop5ByOrderByOrderDateDesc();
}
