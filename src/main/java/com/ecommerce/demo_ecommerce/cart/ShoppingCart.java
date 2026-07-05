package com.ecommerce.demo_ecommerce.cart;

import com.ecommerce.demo_ecommerce.entity.Product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ShoppingCart {

    private List<CartItem> items = new ArrayList<>();

    public void addProduct(Product product) {
        for (CartItem item : items) {
            if (item.getProduct().getId().equals(product.getId())) {
                item.increaseQuantity();
                return;
            }
        }

        items.add(new CartItem(product, 1));
    }

    public void removeProduct(Long productId) {
        items.removeIf(item -> item.getProduct().getId().equals(productId));
    }

    public int getQuantityByProductId(Long productId) {
        int quantity = 0;

        for (CartItem item : items) {
            if (item.getProduct().getId().equals(productId)) {
                quantity += item.getQuantity();
            }
        }

        return quantity;
    }

    public void increaseProduct(Long productId) {
        for (CartItem item : items) {
            if (item.getProduct().getId().equals(productId)) {
                item.increaseQuantity();
                return;
            }
        }
    }

    public void decreaseProduct(Long productId) {
        for (CartItem item : items) {
            if (item.getProduct().getId().equals(productId)) {
                if (item.getQuantity() > 1) {
                    item.decreaseQuantity();
                } else {
                    removeProduct(productId);
                }
                return;
            }
        }
    }

    public List<CartItem> getItems() {
        return items;
    }

    public BigDecimal getTotal() {
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem item : items) {
            total = total.add(item.getSubtotal());
        }

        return total;
    }

    public int getItemCount() {
        int count = 0;

        for (CartItem item : items) {
            count += item.getQuantity();
        }

        return count;
    }
}