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
// 1. **saveOrder Method**:
//    - Processes a customer's order, including saving the order details and associated items.
//    - Parameters: `PlaceOrderRequestDTO placeOrderRequest` (Request data for placing an order)
//    - Return Type: `void` (This method doesn't return anything, it just processes the order)

    @Transactional
    public void saveOrder(PlaceOrderRequestDTO placeOrderRequestDTO) {

        // Retrieve or Create Customer
        Customer customer = customerRepository.findByEmail(placeOrderRequestDTO.getCustomerEmail());
        if(customer == null) {
            customer = new Customer();
            customer.setName(placeOrderRequestDTO.getCustomerName());
            customer.setEmail(placeOrderRequestDTO.getCustomerEmail());
            customer.setPhone(placeOrderRequestDTO.getCustomerPhone());
            customer = customerRepository.save(customer);
        }

        // Retrieve Store
        Optional<Store> storeOptional = storeRepository.findById(placeOrderRequestDTO.getStoreId());
        if (storeOptional.isEmpty()) {
            throw new RuntimeException("Store not found with ID: " + placeOrderRequestDTO.getStoreId());
        }
        Store store = storeOptional.get();

        // Create and save orderdetails
        OrderDetails orderDetails = new OrderDetails();
        orderDetails.setCustomer(customer);
        orderDetails.setStore(store);
        orderDetails.setTotalPrice(placeOrderRequestDTO.getTotalPrice());
        orderDetails.setDate(LocalDateTime.now());

        // save the order
        orderDetails = orderDetailsRepository.save(orderDetails);

        // create and save orderitems and manage stock levels
        // loops through each product item requested in purchase list
        for (var itemRequest : placeOrderRequestDTO.getPurchaseProduct()) {

            // Fetch the specific product object to grab its details/price
            Product product = productRepository.findById(itemRequest.getId()).orElse(null);
            if (product == null) {
                throw new RuntimeException("Product not found with ID: " + itemRequest.getId());
            }

            // Find the inventory record matching this product at this specific store
            Inventory inventory = inventoryRepository.findByProductIdAndStoreId(product.getId(), store.getId());
            if (inventory == null) {
                throw new RuntimeException("Product " + product.getName() + " is not stocked at store " + store.getName());
            }

            // Check if there is enough stock available
            if (inventory.getStockLevel() < itemRequest.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            // Decrease the stock level and save the modified inventory back to the DB
            inventory.setStockLevel(inventory.getStockLevel() - itemRequest.getQuantity());
            inventoryRepository.save(inventory);

            // Create the child OrderItem record linking back to the saved parent OrderDetails
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(orderDetails);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(product.getPrice()); // Capture the historical price at the time of purchase

            // Save each item to the item repository
            orderItemRepository.save(orderItem);
        }
    }

// 2. **Retrieve or Create the Customer**:
//    - Check if the customer exists by their email using `findByEmail`.
//    - If the customer exists, use the existing customer; otherwise, create and save a new customer using `customerRepository.save()`.

// 3. **Retrieve the Store**:
//    - Fetch the store by ID from `storeRepository`.
//    - If the store doesn't exist, throw an exception. Use `storeRepository.findById()`.

// 4. **Create OrderDetails**:
//    - Create a new `OrderDetails` object and set customer, store, total price, and the current timestamp.
//    - Set the order date using `java.time.LocalDateTime.now()` and save the order with `orderDetailsRepository.save()`.

// 5. **Create and Save OrderItems**:
//    - For each product purchased, find the corresponding inventory, update stock levels, and save the changes using `inventoryRepository.save()`.
//    - Create and save `OrderItem` for each product and associate it with the `OrderDetails` using `orderItemRepository.save()`.

   
}
