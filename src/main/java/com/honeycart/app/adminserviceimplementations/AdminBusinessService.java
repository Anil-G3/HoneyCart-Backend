package com.honeycart.app.adminserviceimplementations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.honeycart.app.adminservices.AdminBusinessServiceContract;
import com.honeycart.app.entities.Order;
import com.honeycart.app.entities.OrderItem;
import com.honeycart.app.repositories.OrderItemRepository;
import com.honeycart.app.repositories.OrderRepository;
import com.honeycart.app.repositories.ProductRepository;

@Service
public class AdminBusinessService implements AdminBusinessServiceContract {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    public AdminBusinessService(OrderRepository orderRepository, OrderItemRepository orderItemRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
    }

    @Override
    public Map<String, Object> calculateMonthlyBusiness(int month, int year) {
        List<Order> successfulOrders = orderRepository.findSuccessfulOrdersByMonthAndYear(month, year);
        return calculateBusinessMetrics(successfulOrders);
    }

    @Override
    public Map<String, Object> calculateDailyBusiness(LocalDate date) {
        List<Order> successfulOrders = orderRepository.findSuccessfulOrdersByDate(date);
        return calculateBusinessMetrics(successfulOrders);
    }

    @Override
    public Map<String, Object> calculateYearlyBusiness(int year) {
        List<Order> successfulOrders = orderRepository.findSuccessfulOrdersByYear(year);
        return calculateBusinessMetrics(successfulOrders);
    }

    @Override
    public Map<String, Object> calculateOverallBusiness() {
        List<Order> successfulOrders = orderRepository.findAllByStatus("SUCCESS");
        BigDecimal totalBusiness = orderRepository.calculateOverallBusiness();
        Map<String, Object> response = calculateBusinessMetrics(successfulOrders);
        response.put("totalBusiness", totalBusiness.doubleValue());
        return response;
    }

    @Override
    public Map<String, Object> calculateBusinessMetrics(List<Order> orders) {
        double totalRevenue = 0.0;
        Map<String, Integer> categorySales = new HashMap<>();

        for (Order order : orders) {
            totalRevenue += order.getTotalAmount().doubleValue();

            List<OrderItem> items = orderItemRepository.findByOrderId(order.getOrderId());
            for (OrderItem item : items) {
                String categoryName = productRepository.findCategoryNameByProductId(item.getProductId());
                categorySales.put(categoryName, categorySales.getOrDefault(categoryName, 0) + item.getQuantity());
            }
        }

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalRevenue", totalRevenue);
        metrics.put("categorySales", categorySales);
        return metrics;
    }
	
}
