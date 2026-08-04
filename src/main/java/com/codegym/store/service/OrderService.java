package com.codegym.store.service;

import com.codegym.store.model.Order;
import com.codegym.store.model.OrderStatus;
import com.codegym.store.model.User;

import java.util.List;

public interface OrderService {
    Order createOrderFromCart(User user, String receiverName, String receiverPhone, String receiverEmail, String shippingAddress);
    void updateOrderStatus(Long orderId, OrderStatus newStatus);
    List<Order> getUserOrders(Long userId);
    List<Order> getActiveUserOrders(Long userId);
    List<Order> getCompletedUserOrders(Long userId);
    List<Order> getAllOrders();
    Order getOrderById(Long orderId);
}
