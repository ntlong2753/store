package com.codegym.store.service.impl;

import com.codegym.store.model.*;
import com.codegym.store.repository.*;
import com.codegym.store.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserAddressRepository userAddressRepository;

    public OrderServiceImpl(OrderRepository orderRepository, CartRepository cartRepository, ProductRepository productRepository, UserAddressRepository userAddressRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userAddressRepository = userAddressRepository;
    }

    @Override
    @Transactional
    public Order createOrderFromCart(User user, String receiverName, String receiverPhone, String receiverEmail, String shippingAddress) {
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Giỏ hàng trống!"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Giỏ hàng không có sản phẩm nào!");
        }

        // Lưu địa chỉ mới vào profile nếu chưa có địa chỉ mặc định
        Optional<UserAddress> defaultAddress = userAddressRepository.findByUserIdAndIsDefaultTrue(user.getId());
        if (defaultAddress.isEmpty()) {
            UserAddress newAddress = new UserAddress();
            newAddress.setUser(user);
            newAddress.setReceiverName(receiverName);
            newAddress.setPhone(receiverPhone);
            newAddress.setFullAddress(shippingAddress);
            newAddress.setDefault(true);
            userAddressRepository.save(newAddress);
        } else {
            // Cập nhật địa chỉ mặc định theo thông tin mới nhất đặt hàng (Optionally)
            UserAddress address = defaultAddress.get();
            address.setReceiverName(receiverName);
            address.setPhone(receiverPhone);
            address.setFullAddress(shippingAddress);
            userAddressRepository.save(address);
        }

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setReceiverName(receiverName);
        order.setReceiverPhone(receiverPhone);
        order.setReceiverEmail(receiverEmail);
        order.setShippingAddress(shippingAddress);
        order.setTotalPrice(cart.getTotalPrice());

        // Chuyển CartItem sang OrderItem và trừ Stock
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            if (product.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException("Sản phẩm " + product.getName() + " không đủ số lượng tồn kho!");
            }

            // Trừ stock
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(product.getPrice());
            order.getItems().add(orderItem);
        }

        Order savedOrder = orderRepository.save(order);

        // Xóa sạch giỏ hàng
        cart.getItems().clear();
        cartRepository.save(cart);

        return savedOrder;
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        // Nếu trạng thái mới là REJECTED (Hủy/Không duyệt) thì hoàn lại Stock
        if (newStatus == OrderStatus.REJECTED && order.getStatus() != OrderStatus.REJECTED) {
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            }
        }
        // Nếu từ REJECTED chuyển sang trạng thái khác (duyệt lại) thì trừ Stock lại
        else if (order.getStatus() == OrderStatus.REJECTED && newStatus != OrderStatus.REJECTED) {
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                if (product.getStock() < item.getQuantity()) {
                    throw new RuntimeException("Không đủ tồn kho để khôi phục đơn hàng!");
                }
                product.setStock(product.getStock() - item.getQuantity());
                productRepository.save(product);
            }
        }

        order.setStatus(newStatus);
        orderRepository.save(order);
    }

    @Override
    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByOrderDateDesc(userId);
    }

    @Override
    public List<Order> getActiveUserOrders(Long userId) {
        return orderRepository.findByUserIdAndStatusInOrderByOrderDateDesc(userId, List.of(OrderStatus.PENDING, OrderStatus.APPROVED, OrderStatus.SHIPPING));
    }

    @Override
    public List<Order> getCompletedUserOrders(Long userId) {
        return orderRepository.findByUserIdAndStatusInOrderByOrderDateDesc(userId, List.of(OrderStatus.DELIVERED, OrderStatus.REJECTED));
    }


    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }
}
