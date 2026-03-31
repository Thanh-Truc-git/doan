package com.example.doanck.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.doanck.model.Combo;
import com.example.doanck.model.Food;
import com.example.doanck.model.Order;
import com.example.doanck.model.OrderDetail;
import com.example.doanck.model.Promotion;
import com.example.doanck.model.User;
import com.example.doanck.repository.ComboRepository;
import com.example.doanck.repository.FoodRepository;
import com.example.doanck.repository.OrderDetailRepository;
import com.example.doanck.repository.OrderRepository;
import com.example.doanck.repository.PromotionRepository;

import jakarta.transaction.Transactional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private ComboRepository comboRepository;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private PromotionService promotionService;

    // ===== GET ORDERS =====
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public List<Order> getUserOrders(User user) {
        return orderRepository.findByUserOrderByOrderTimeDesc(user);
    }

    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    // ===== CREATE ORDER =====
    @Transactional
    public Order createOrder(User user, String promotionCode) {
        Order order = new Order();
        order.setUser(user);

        // Nếu có mã khuyến mãi
        if (promotionCode != null && !promotionCode.isEmpty()) {
            if (promotionService.validatePromotion(promotionCode)) {
                Optional<Promotion> promotion = promotionRepository.findByCode(promotionCode);
                if (promotion.isPresent()) {
                    order.setPromotion(promotion.get());
                }
            }
        }

        return orderRepository.save(order);
    }

    // ===== CREATE ORDER FROM MOVIE (MVC) =====
    @Transactional
    public Order createOrderWithMovie(User user, Long movieId) {

        Order order = new Order();
        order.setUser(user);

        // giữ logic mặc định
        order.setStatus("PENDING");

        // ⚠️ hiện tại bạn chưa có Movie trong OrderDetail
        // nên tạm set tiền mặc định (tránh null lỗi)
        order.setTotalAmount(0);
        order.setFinalAmount(0);

        return orderRepository.save(order);
    }
    // ===== ADD FOOD TO ORDER =====
    @Transactional
    public OrderDetail addFoodToOrder(Long orderId, Long foodId, int quantity, String notes) {
        Optional<Order> order = orderRepository.findById(orderId);
        Optional<Food> food = foodRepository.findById(foodId);

        if (order.isPresent() && food.isPresent()) {
            OrderDetail orderDetail = new OrderDetail(order.get(), food.get(), quantity);
            orderDetail.setNotes(notes);
            orderDetail.calculateSubtotal();

            OrderDetail savedDetail = orderDetailRepository.save(orderDetail);
            
            // Cập nhật tổng tiền đơn hàng
            order.get().calculateTotal();
            orderRepository.save(order.get());

            return savedDetail;
        }
        return null;
    }

    // ===== ADD COMBO TO ORDER =====
    @Transactional
    public OrderDetail addComboToOrder(Long orderId, Long comboId, int quantity, String notes) {
        Optional<Order> order = orderRepository.findById(orderId);
        Optional<Combo> combo = comboRepository.findById(comboId);

        if (order.isPresent() && combo.isPresent()) {
            OrderDetail orderDetail = new OrderDetail(order.get(), combo.get(), quantity);
            orderDetail.setNotes(notes);
            orderDetail.calculateSubtotal();

            OrderDetail savedDetail = orderDetailRepository.save(orderDetail);
            
            // Cập nhật tổng tiền đơn hàng
            order.get().calculateTotal();
            orderRepository.save(order.get());

            return savedDetail;
        }
        return null;
    }

    // ===== REMOVE ITEM FROM ORDER =====
    @Transactional
    public void removeOrderDetail(Long orderId, Long orderDetailId) {
        Optional<OrderDetail> orderDetail = orderDetailRepository.findById(orderDetailId);
        if (orderDetail.isPresent()) {
            orderDetailRepository.deleteById(orderDetailId);

            // Cập nhật tổng tiền đơn hàng
            Optional<Order> order = orderRepository.findById(orderId);
            if (order.isPresent()) {
                order.get().calculateTotal();
                orderRepository.save(order.get());
            }
        }
    }

    // ===== UPDATE QUANTITY =====
    @Transactional
    public OrderDetail updateOrderDetailQuantity(Long orderDetailId, int quantity) {
        Optional<OrderDetail> orderDetail = orderDetailRepository.findById(orderDetailId);
        if (orderDetail.isPresent()) {
            OrderDetail detail = orderDetail.get();
            detail.setQuantity(quantity);
            detail.calculateSubtotal();

            OrderDetail updatedDetail = orderDetailRepository.save(detail);
            
            // Cập nhật tổng tiền đơn hàng
            Order order = detail.getOrder();
            order.calculateTotal();
            orderRepository.save(order);

            return updatedDetail;
        }
        return null;
    }

    // ===== APPLY PROMOTION =====
    @Transactional
    public Order applyPromotion(Long orderId, String promotionCode) {
        Optional<Order> order = orderRepository.findById(orderId);

        if (order.isPresent()) {
            if (promotionService.validatePromotion(promotionCode)) {
                Optional<Promotion> promotion = promotionRepository.findByCode(promotionCode);
                if (promotion.isPresent()) {
                    order.get().setPromotion(promotion.get());
                    order.get().calculateTotal();
                    return orderRepository.save(order.get());
                }
            }
        }
        return null;
    }

    // ===== REMOVE PROMOTION =====
    @Transactional
    public Order removePromotion(Long orderId) {
        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isPresent()) {
            order.get().setPromotion(null);
            order.get().calculateTotal();
            return orderRepository.save(order.get());
        }
        return null;
    }

    // ===== CONFIRM & PAY ORDER =====
    @Transactional
    public Order confirmAndPayOrder(Long orderId) {
        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isPresent()) {
            Order o = order.get();
            o.setStatus("PAID");

            // Tăng lượt sử dụng khuyến mãi
            if (o.getPromotion() != null) {
                promotionService.incrementUsageCount(o.getPromotion().getId());
            }

            return orderRepository.save(o);
        }
        return null;
    }

    // ===== CANCEL ORDER =====
    @Transactional
    public Order cancelOrder(Long orderId) {
        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isPresent()) {
            Order o = order.get();
            o.setStatus("CANCELLED");
            return orderRepository.save(o);
        }
        return null;
    }

    // ===== COMPLETE ORDER =====
    @Transactional
    public Order completeOrder(Long orderId) {
        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isPresent()) {
            Order o = order.get();
            o.setStatus("COMPLETED");
            return orderRepository.save(o);
        }
        return null;
    }

    // ===== GET ORDER DETAILS =====
    public List<OrderDetail> getOrderDetails(Long orderId) {
        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isPresent()) {
            return orderDetailRepository.findByOrder(order.get());
        }
        return List.of();
    }
}