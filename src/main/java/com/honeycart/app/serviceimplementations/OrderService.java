package com.honeycart.app.serviceimplementations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.honeycart.app.entities.OrderItem;
import com.honeycart.app.entities.Product;
import com.honeycart.app.entities.ProductImage;
import com.honeycart.app.entities.User;
import com.honeycart.app.repositories.OrderItemRepository;
import com.honeycart.app.repositories.ProductImageRepository;
import com.honeycart.app.repositories.ProductRepository;
import com.honeycart.app.services.OrderServiceContract;

@Service
public class OrderService implements OrderServiceContract {
	
    private OrderItemRepository orderItemRepository;
    private ProductRepository productRepository;
    private ProductImageRepository productImageRepository;
    

    public OrderService(OrderItemRepository orderItemRepository, ProductRepository productRepository, ProductImageRepository productImageRepository) {
		super();
		this.orderItemRepository = orderItemRepository;
		this.productRepository = productRepository;
		this.productImageRepository = productImageRepository;
	}

	/**
     * Fetches all successful orders for a given user and returns the
     * required response format.
     *
     * @param user The authenticated user object.
     * @return A Map containing the user's role, username, and ordered
     * products.
     */
    
    @Override
    public Map<String, Object> getOrdersForUser(User user) {
        List<OrderItem> orderItems = orderItemRepository.findSuccessfulOrderItemsByUserId(user.getUserId());

        Map<String, Object> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("role", user.getRole());

        // Bulk-fetch all products for these order items in ONE query
        List<Integer> productIds = orderItems.stream()
                .map(OrderItem::getProductId)
                .distinct()
                .collect(Collectors.toList());

        Map<Integer, Product> productsById = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getProductId, p -> p));

        // Bulk-fetch the first image for each product in ONE query
        Map<Integer, String> firstImageByProduct = new HashMap<>();
        for (ProductImage img : productImageRepository.findByProduct_ProductIdIn(productIds)) {
            firstImageByProduct.putIfAbsent(img.getProduct().getProductId(), img.getImageUrl());
        }

        List<Map<String, Object>> products = new ArrayList<>();
        for (OrderItem item : orderItems) {
            Product product = productsById.get(item.getProductId());
            if (product == null) {
                continue;
            }
            String imageUrl = firstImageByProduct.get(product.getProductId());

            Map<String, Object> productDetails = new HashMap<>();
            productDetails.put("order_id", item.getOrder().getOrderId());
            productDetails.put("quantity", item.getQuantity());
            productDetails.put("total_price", item.getTotalPrice());
            productDetails.put("image_url", imageUrl);
            productDetails.put("product_id", product.getProductId());
            productDetails.put("name", product.getName());
            productDetails.put("description", product.getDescription());
            productDetails.put("price_per_unit", item.getPricePerUnit());
            products.add(productDetails);
        }

        response.put("products", products);
        return response;
    }
}
