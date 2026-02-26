package com.honeycart.app.serviceimplementations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        // Fetch all successful order items for the user
        List<OrderItem> orderItems = orderItemRepository.findSuccessfulOrderItemsByUserId(user.getUserId());

        // Prepare the response map
        Map<String, Object> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("role", user.getRole()); // Directly use the role as it is an enum mapped to a string

        // Transform order items into a list of product details
        List<Map<String, Object>> products = new ArrayList<>();
        for (OrderItem item : orderItems) {
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            if (product == null) {
                continue; // Skip if the product does not exist
            }

            // Fetch the product image (if available)
            List<ProductImage> images = productImageRepository.findByProduct_ProductId(product.getProductId());
            String imageUrl = images.isEmpty() ? null : images.get(0).getImageUrl();

            // Create a product details map
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

        // Add the products list to the response
        response.put("products", products);

        return response;
    }
}
