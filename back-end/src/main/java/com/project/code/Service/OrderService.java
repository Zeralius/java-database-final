package com.project.code.Service;


import com.project.code.Model.*;
import com.project.code.Repo.*;
import jakarta.transaction.Transactional;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OrderService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private OrderDetailsRepository orderDetailsRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;


    @Transactional
    public void saveOrder(PlaceOrderRequestDTO placeOrderRequestDTO) {


        Customer customer = customerRepository.findByEmail(placeOrderRequestDTO.getCustomerEmail());
        if(customer == null) {
            customer = new Customer();
            customer.setName(placeOrderRequestDTO.getCustomerName());
            customer.setEmail(placeOrderRequestDTO.getCustomerEmail());
            customer.setPhone(placeOrderRequestDTO.getCustomerPhone());
            customer = customerRepository.save(customer);
        }

        Optional<Store> storeOptional = storeRepository.findById(placeOrderRequestDTO.getStoreId());
        if (storeOptional.isEmpty()) {
            throw new RuntimeException("Store not found with ID: " + placeOrderRequestDTO.getStoreId());
        }
        Store store = storeOptional.get();


        OrderDetails orderDetails = new OrderDetails();
        orderDetails.setCustomer(customer);
        orderDetails.setStore(store);
        orderDetails.setTotalPrice(placeOrderRequestDTO.getTotalPrice());
        orderDetails.setDate(LocalDateTime.now());


        orderDetails = orderDetailsRepository.save(orderDetails);


        for (var itemRequest : placeOrderRequestDTO.getPurchaseProduct()) {


            Product product = productRepository.findById(itemRequest.getId()).orElse(null);
            if (product == null) {
                throw new RuntimeException("Product not found with ID: " + itemRequest.getId());
            }


            Inventory inventory = inventoryRepository.findByProductIdAndStoreId(product.getId(), store.getId());
            if (inventory == null) {
                throw new RuntimeException("Product " + product.getName() + " is not stocked at store " + store.getName());
            }


            if (inventory.getStockLevel() < itemRequest.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }


            inventory.setStockLevel(inventory.getStockLevel() - itemRequest.getQuantity());
            inventoryRepository.save(inventory);


            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(orderDetails);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());



            orderItemRepository.save(orderItem);

            // FIXED: Explicitly save the updated inventory object back to the database
            inventoryRepository.save(inventory);
        }
    }
   
}
