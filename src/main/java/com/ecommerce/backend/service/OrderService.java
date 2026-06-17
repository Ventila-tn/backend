package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.CheckoutRequest;
import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.OrderItem;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.enums.OrderStatus;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final StockService stockService;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository, StockService stockService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.stockService = stockService;
    }

    @Transactional
    public Order placeOrder(CheckoutRequest request) {
        Order order = new Order();
        order.setFirstName(request.firstName());
        order.setLastName(request.lastName());
        order.setAddress(request.address());
        order.setPhone(request.phone());
        order.setEmail(request.email());
        order.setStatus(OrderStatus.PENDING_CONFIRMATION);
        order.setHasStockShortage(false);

        List<OrderItem> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry : request.items().entrySet()) {
            Product product = productRepository.findById(entry.getKey())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + entry.getKey()));

            Integer stock = stockService.getTotalStock(product.getId());
            Integer requestedQuantity = entry.getValue();

            if (stock < requestedQuantity) {
                order.setHasStockShortage(true);
            }

            if (stock > 0) {
                int quantityToDeduct = Math.min(stock, requestedQuantity);
                stockService.registerOutboundMovement(product, quantityToDeduct, "Order placed");
            }

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(requestedQuantity);
            item.setUnitPrice(product.getSellingPriceTTC());

            BigDecimal itemTotal = product.getSellingPriceTTC().multiply(BigDecimal.valueOf(requestedQuantity));
            item.setTotalPrice(itemTotal);

            items.add(item);
            totalAmount = totalAmount.add(itemTotal);
        }

        order.setItems(items);
        order.setTotalAmount(totalAmount);

        return orderRepository.save(order);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc();
    }

    public Order updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        return orderRepository.save(order);
    }
}
