package com.honeycart.app.serviceimplementations;

import java.math.BigDecimal;

import java.time.LocalDateTime;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.honeycart.app.entities.CartItem;
import com.honeycart.app.entities.Order;
import com.honeycart.app.entities.OrderItem;
import com.honeycart.app.entities.OrderStatus;
import com.honeycart.app.repositories.CartRepository;
import com.honeycart.app.repositories.OrderItemRepository;
import com.honeycart.app.repositories.OrderRepository;
import com.honeycart.app.services.PaymentServiceContract;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import jakarta.transaction.Transactional;

@Service
public class PaymentService implements PaymentServiceContract {
	
	@Value("${razorpay.key_id}")
	private String razorpayKeyId;
	
	@Value("${razorpay.key_secret}")
	private String razorpayKeySecret;
	
	private OrderRepository orderRepository;
	private OrderItemRepository orderItemRepository;
	private CartRepository cartRepository;
	
	public PaymentService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
			CartRepository cartRepository) {
		super();
		this.orderRepository = orderRepository;
		this.orderItemRepository = orderItemRepository;
		this.cartRepository = cartRepository;
	}

	@Override
	@Transactional
	public String createOrder(int userId, BigDecimal totalAmount, List<OrderItem> cartItems) throws RazorpayException {

		// Create Razorpay client
		RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
		
		// Prepare Razorpay order request
		var orderRequest = new JSONObject();
		orderRequest.put("amount", totalAmount.multiply(BigDecimal.valueOf(100)).intValue()); // Amount in paise
		orderRequest.put("currency", "INR");
		orderRequest.put("receipt", "txn_" + System.currentTimeMillis());
		
		// Create Razorpay order
		com.razorpay.Order razorpayOrder = razorpayClient.orders.create(orderRequest);
		
		// Save order details in the database
		Order order = new Order();
		order.setOrderId(razorpayOrder.get("id"));
		order.setUserId(userId);
		order.setTotalAmount(totalAmount);
		order.setStatus(OrderStatus.PENDING);
		order.setCreatedAt(LocalDateTime.now());
		orderRepository.save(order);

		return razorpayOrder.get("id");
	}

	@Override
	@Transactional
	public boolean verifyPayment(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature, int userId) {

		try
		{
		
			// Prepare signature validation attributes
			JSONObject attributes = new JSONObject();
			attributes.put("razorpay_order_id", razorpayOrderId);
			attributes.put("razorpay_payment_id", razorpayPaymentId);
			attributes.put("razorpay_signature", razorpaySignature);
			
			// Verify razorpay signature
			boolean isSignatureValid = com.razorpay.Utils.verifyPaymentSignature(attributes, razorpayKeySecret);
			
			if (isSignatureValid) {
				// Update order status to SUCCESS
				Order order = orderRepository.findById(razorpayOrderId).orElseThrow(() -> new RuntimeException("Order not found"));
				order.setStatus(OrderStatus.SUCCESS);
				order.setUpdatedAt(LocalDateTime.now());
				orderRepository.save(order);
				
				// Fetch cart items for the user
				List<CartItem> cartItems = cartRepository.findCartItemsWithProductDetails(userId);
				
				// Save order items
				for (CartItem cartItem : cartItems) {
					OrderItem orderItem = new OrderItem();
					orderItem.setOrder(order);
					orderItem.setProductId(cartItem.getProduct().getProductId());
					orderItem.setQuantity(cartItem.getQuantity());
					orderItem.setPricePerUnit(cartItem.getProduct().getPrice());
					
					orderItem.setTotalPrice(cartItem.getProduct().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
					orderItemRepository.save(orderItem);
				}
				
				// Clear users cart
				cartRepository.deleteAllCartItemsByUserId(userId);
				
				return true;
				
			} else {
				return false;
			}
			
			
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}


}
