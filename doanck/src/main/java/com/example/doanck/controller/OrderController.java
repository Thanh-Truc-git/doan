package com.example.doanck.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.doanck.model.Order;
import com.example.doanck.model.OrderDetail;
import com.example.doanck.model.User;
import com.example.doanck.repository.UserRepository;
import com.example.doanck.service.ComboService;
import com.example.doanck.service.FoodService;
import com.example.doanck.service.OrderService;
import com.example.doanck.service.PromotionService;

@RestController
@RequestMapping("/api/order")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private FoodService foodService;

    @Autowired
    private ComboService comboService;

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private UserRepository userRepository;

    // ===== GET ALL ORDERS =====
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    // ===== GET ORDER BY ID =====
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        Optional<Order> order = orderService.getOrderById(id);
        if (order.isPresent()) {
            return ResponseEntity.ok(order.get());
        }
        return ResponseEntity.notFound().build();
    }

    // ===== GET USER ORDERS =====
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getUserOrders(@PathVariable Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            List<Order> orders = orderService.getUserOrders(user.get());
            return ResponseEntity.ok(orders);
        }
        return ResponseEntity.notFound().build();
    }

    // ===== GET ORDERS BY STATUS =====
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable String status) {
        List<Order> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    // ===== CREATE NEW ORDER =====
    @PostMapping("/create/{userId}")
    public ResponseEntity<?> createOrder(
            @PathVariable Long userId,
            @RequestParam(required = false) String promotionCode) {

        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(createResponse("error", "User not found"));
        }

        Order order = orderService.createOrder(user.get(), promotionCode);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createResponse("success", "Order created", order));
    }

    // ===== ADD FOOD TO ORDER =====
    @PostMapping("/{orderId}/add-food")
    public ResponseEntity<?> addFoodToOrder(
            @PathVariable Long orderId,
            @RequestParam Long foodId,
            @RequestParam int quantity,
            @RequestParam(required = false) String notes) {

        OrderDetail orderDetail = orderService.addFoodToOrder(orderId, foodId, quantity, notes);
        if (orderDetail != null) {
            return ResponseEntity.ok(createResponse("success", "Food added to order", orderDetail));
        }
        return ResponseEntity.badRequest()
                .body(createResponse("error", "Failed to add food to order"));
    }

    // ===== ADD COMBO TO ORDER =====
    @PostMapping("/{orderId}/add-combo")
    public ResponseEntity<?> addComboToOrder(
            @PathVariable Long orderId,
            @RequestParam Long comboId,
            @RequestParam int quantity,
            @RequestParam(required = false) String notes) {

        OrderDetail orderDetail = orderService.addComboToOrder(orderId, comboId, quantity, notes);
        if (orderDetail != null) {
            return ResponseEntity.ok(createResponse("success", "Combo added to order", orderDetail));
        }
        return ResponseEntity.badRequest()
                .body(createResponse("error", "Failed to add combo to order"));
    }

    // ===== REMOVE ITEM FROM ORDER =====
    @DeleteMapping("/{orderId}/remove-detail/{detailId}")
    public ResponseEntity<?> removeOrderDetail(
            @PathVariable Long orderId,
            @PathVariable Long detailId) {

        orderService.removeOrderDetail(orderId, detailId);
        return ResponseEntity.ok(createResponse("success", "Item removed from order"));
    }

    // ===== UPDATE QUANTITY =====
    @PutMapping("/detail/{detailId}/quantity")
    public ResponseEntity<?> updateQuantity(
            @PathVariable Long detailId,
            @RequestParam int quantity) {

        if (quantity <= 0) {
            return ResponseEntity.badRequest()
                    .body(createResponse("error", "Quantity must be greater than 0"));
        }

        OrderDetail updatedDetail = orderService.updateOrderDetailQuantity(detailId, quantity);
        if (updatedDetail != null) {
            return ResponseEntity.ok(createResponse("success", "Quantity updated", updatedDetail));
        }
        return ResponseEntity.badRequest()
                .body(createResponse("error", "Failed to update quantity"));
    }

    // ===== APPLY PROMOTION =====
    @PostMapping("/{orderId}/apply-promotion")
    public ResponseEntity<?> applyPromotion(
            @PathVariable Long orderId,
            @RequestParam String promotionCode) {

        if (!promotionService.validatePromotion(promotionCode)) {
            return ResponseEntity.badRequest()
                    .body(createResponse("error", "Invalid or expired promotion code"));
        }

        Order order = orderService.applyPromotion(orderId, promotionCode);
        if (order != null) {
            return ResponseEntity.ok(createResponse("success", "Promotion applied", order));
        }
        return ResponseEntity.badRequest()
                .body(createResponse("error", "Failed to apply promotion"));
    }

    // ===== REMOVE PROMOTION =====
    @PostMapping("/{orderId}/remove-promotion")
    public ResponseEntity<?> removePromotion(@PathVariable Long orderId) {
        Order order = orderService.removePromotion(orderId);
        if (order != null) {
            return ResponseEntity.ok(createResponse("success", "Promotion removed", order));
        }
        return ResponseEntity.badRequest()
                .body(createResponse("error", "Failed to remove promotion"));
    }

    // ===== CONFIRM & PAY =====
    @PostMapping("/{orderId}/pay")
    public ResponseEntity<?> payOrder(@PathVariable Long orderId) {
        Order order = orderService.confirmAndPayOrder(orderId);
        if (order != null) {
            return ResponseEntity.ok(createResponse("success", "Order paid successfully", order));
        }
        return ResponseEntity.badRequest()
                .body(createResponse("error", "Failed to pay order"));
    }

    // ===== CANCEL ORDER =====
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId) {
        Order order = orderService.cancelOrder(orderId);
        if (order != null) {
            return ResponseEntity.ok(createResponse("success", "Order cancelled", order));
        }
        return ResponseEntity.badRequest()
                .body(createResponse("error", "Failed to cancel order"));
    }

    // ===== COMPLETE ORDER =====
    @PostMapping("/{orderId}/complete")
    public ResponseEntity<?> completeOrder(@PathVariable Long orderId) {
        Order order = orderService.completeOrder(orderId);
        if (order != null) {
            return ResponseEntity.ok(createResponse("success", "Order completed", order));
        }
        return ResponseEntity.badRequest()
                .body(createResponse("error", "Failed to complete order"));
    }

    // ===== GET ORDER DETAILS =====
    @GetMapping("/{orderId}/details")
    public ResponseEntity<List<OrderDetail>> getOrderDetails(@PathVariable Long orderId) {
        List<OrderDetail> details = orderService.getOrderDetails(orderId);
        return ResponseEntity.ok(details);
    }

    // ===== HELPER METHOD =====
    private Map<String, Object> createResponse(String status, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", status);
        response.put("message", message);
        return response;
    }

    private Map<String, Object> createResponse(String status, String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", status);
        response.put("message", message);
        response.put("data", data);
        return response;
    }
}