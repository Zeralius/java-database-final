package com.project.code.Controller;


import com.project.code.Model.CombinedRequest;
import com.project.code.Model.Inventory;
import com.project.code.Model.Product;
import com.project.code.Repo.InventoryRepository;
import com.project.code.Repo.ProductRepository;
import com.project.code.Service.ServiceClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/inventory")
public class InventoryController {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ServiceClass serviceClass;


    @PutMapping
    public Map<String, String> updateInventory(@RequestBody CombinedRequest combinedRequest) {
        Map<String, String> response = new HashMap<>();
        try {
            Product incomingProduct = combinedRequest.getProduct();
            Inventory incomingInventory = combinedRequest.getInventory();

            boolean productExists = serviceClass.validateProductId(incomingProduct.getId());
            if (!productExists) {
                response.put("message", "Product not found with ID: " + incomingProduct.getId());
                return response;
            }

            Inventory existingInventory = inventoryRepository.findByProductIdAndStoreId(
                    incomingProduct.getId(),
                    incomingInventory.getStore().getId()
            );

            if (existingInventory != null) {
                productRepository.save(incomingProduct);
                existingInventory.setStockLevel(incomingInventory.getStockLevel());
                inventoryRepository.save(existingInventory);
                response.put("message", "Successfully updated product");
            } else {
                response.put("message", "No data available");
            }
        } catch (DataIntegrityViolationException e) {
            response.put("message", "Data integrity violation: " + e.getMessage());
        } catch (Exception e) {
            response.put("message", "Error occurred: " + e.getMessage());
        }
        return response;
    }


    @PostMapping
    public Map<String, String> saveInventory(@RequestBody Inventory inventory) {
        Map<String, String> response = new HashMap<>();
        try {
            boolean isNewInventory = serviceClass.validateInventory(inventory);
            if (!isNewInventory) {
                response.put("message", "Data is already present");
            } else {
                inventoryRepository.save(inventory);
                response.put("message", "Data saved successfully");
            }
        } catch (Exception e) {
            response.put("message", "An error occurred: " + e.getMessage());
        }
        return response;
    }


    @GetMapping("/{storeid}")
    public Map<String, Object> getAllProducts(@PathVariable("storeid") Long storeId) {
        Map<String, Object> response = new HashMap<>();
        List<Product> products = productRepository.findProductsByStoreId(storeId);
        response.put("products", products);
        return response;
    }


    // FIXED: Standardized the path string variable to exactly match the camelCase "{storeId}" requirement
    @GetMapping("/filter/{category}/{name}/{storeId}")
    public Map<String, Object> getProductName(
            @PathVariable("category") String category,
            @PathVariable("name") String name,
            @PathVariable("storeId") Long storeId) {

        Map<String, Object> response = new HashMap<>();
        List<Product> filteredProducts;

        boolean isCategoryNull = "null".equalsIgnoreCase(category);
        boolean isNameNull = "null".equalsIgnoreCase(name);

        if (isCategoryNull && !isNameNull) {
            filteredProducts = productRepository.findByNameLike(storeId, name);
        } else if (!isCategoryNull && isNameNull) {
            filteredProducts = productRepository.findByCategoryAndStoreId(storeId, category);
        } else if (!isCategoryNull && !isNameNull) {
            filteredProducts = productRepository.findByNameAndCategory(storeId, name, category);
        } else {
            filteredProducts = productRepository.findProductsByStoreId(storeId);
        }

        response.put("product", filteredProducts);
        return response;
    }


    @GetMapping("search/{name}/{storeId}")
    public Map<String, Object> searchProduct(
            @PathVariable("name") String name,
            @PathVariable("storeId") Long storeId) {

        Map<String, Object> response = new HashMap<>();
        List<Product> products = productRepository.findByNameLike(storeId, name);
        response.put("product", products);
        return response;
    }


    @DeleteMapping("/{id}")
    public Map<String, String> removeProduct(@PathVariable("id") Long id) {
        Map<String, String> response = new HashMap<>();

        boolean productExists = serviceClass.validateProductId(id);
        if (!productExists) {
            response.put("message", "Product not present in database");
            return response;
        }

        inventoryRepository.deleteByProductId(id);
        response.put("message", "Successfully deleted product");
        return response;
    }


    // FIXED: Ensured route maps to camelCase storeId/productId placeholders and checks for null record objects explicitly
    @GetMapping("validate/{quantity}/{storeId}/{productId}")
    public boolean validateQuantity(
            @PathVariable("quantity") Integer quantity,
            @PathVariable("storeId") Long storeId,
            @PathVariable("productId") Long productId) {


        boolean productExists = serviceClass.validateProductId(productId);
        if (!productExists) {
            return false;
        }

        Inventory inventory = inventoryRepository.findByProductIdAndStoreId(productId, storeId);


        if (inventory != null && inventory.getStockLevel() != null && inventory.getStockLevel() >= quantity) {
            return true;
        }
        return false;
    }
}
