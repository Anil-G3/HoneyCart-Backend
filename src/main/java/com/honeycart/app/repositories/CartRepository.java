package com.honeycart.app.repositories;

import java.util.List;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.honeycart.app.entities.CartItem;

import jakarta.transaction.Transactional;

@Repository
public interface CartRepository extends JpaRepository<CartItem, Integer> {

    // Fetch cart item for a given userId and productId
    @Query("SELECT c FROM CartItem c WHERE c.user.userId = :userId AND c.product.productId = :productId")
    Optional<CartItem> findByUserAndProduct(int userId, int productId);

    @Query("SELECT c FROM CartItem c JOIN FETCH c.product p LEFT JOIN FETCH ProductImage pi ON p.productId = pi.product.productId WHERE c.user.userId = :userId")
    List<CartItem> findCartItemsWithProductDetails(int userId);

    // Update quantity for a specific cart item
    @Modifying
    @Transactional
    @Query("UPDATE CartItem c SET c.quantity = :quantity WHERE c.id = :cartItemId")
    void updateCartItemQuantity(int cartItemId, int quantity);

    // Delete a product from the cart
    @Modifying
    @Transactional
    @Query("DELETE FROM CartItem c WHERE c.user.userId = :userId AND c.product.productId = :productId")
    void deleteCartItem(int userId, int productId);

    // Count the total quantity of items in the cart
    @Query("SELECT COALESCE(SUM(c.quantity), 0) FROM CartItem c WHERE c.user.userId = :userId")
    int countTotalItems(int userId);
    
    
    @Modifying
    @Transactional
    @Query("DELETE FROM CartItem c WHERE c.user.userId = :userId")
    void deleteAllCartItemsByUserId(int userId);	
    
	// Custom query to count cart items for a given userId
	@Query("SELECT COUNT(c) FROM CartItem c WHERE c.user.id = :userId")
	int countCartItemsByUserId(@Param("userId") int userId);
	
	// Custom query to fetch userId by username (assumes relationship exists)
	@Query("SELECT u.id FROM User u WHERE u.username = :username")
	int findUserIdByUsername(@Param("username") String username);
	
	@Modifying
	@Transactional
	@Query("DELETE FROM CartItem c WHERE c.product.productId = :productId")
	void deleteByProductId(@Param("productId") int productId);
	
}


