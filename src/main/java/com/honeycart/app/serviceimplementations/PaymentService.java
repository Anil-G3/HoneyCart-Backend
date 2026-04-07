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

	    RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
	    
	    // If the cart total is above the 25k limit (razorpay gives a limit of 25,000 only for test mode, unable to process payment if limit exceeds), 
	    // so we tell Razorpay the order is only for 100.
	    BigDecimal amountForRazorpay = totalAmount.compareTo(new BigDecimal("20000")) > 0 
	            ? new BigDecimal("100") 
	            : totalAmount;

	    var orderRequest = new JSONObject();
	    orderRequest.put("amount", amountForRazorpay.multiply(BigDecimal.valueOf(100)).intValue()); // Amount in paise
	    orderRequest.put("currency", "INR");
	    orderRequest.put("receipt", "txn_" + System.currentTimeMillis());

	    com.razorpay.Order razorpayOrder = razorpayClient.orders.create(orderRequest);

	    // saving the ACTUAL amount
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

			JSONObject attributes = new JSONObject();
			attributes.put("razorpay_order_id", razorpayOrderId);
			attributes.put("razorpay_payment_id", razorpayPaymentId);
			attributes.put("razorpay_signature", razorpaySignature);

			boolean isSignatureValid = com.razorpay.Utils.verifyPaymentSignature(attributes, razorpayKeySecret);
			
			if (isSignatureValid) {
				Order order = orderRepository.findById(razorpayOrderId).orElseThrow(() -> new RuntimeException("Order not found"));
				order.setStatus(OrderStatus.SUCCESS);
				order.setUpdatedAt(LocalDateTime.now());
				orderRepository.save(order);

				List<CartItem> cartItems = cartRepository.findCartItemsWithProductDetails(userId);

				for (CartItem cartItem : cartItems) {
					OrderItem orderItem = new OrderItem();
					orderItem.setOrder(order);
					orderItem.setProductId(cartItem.getProduct().getProductId());
					orderItem.setQuantity(cartItem.getQuantity());
					orderItem.setPricePerUnit(cartItem.getProduct().getPrice());
					
					orderItem.setTotalPrice(cartItem.getProduct().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
					orderItemRepository.save(orderItem);
				}

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
