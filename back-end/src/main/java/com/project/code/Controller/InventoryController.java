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
        } catch (DataIntegrityViolationException e) {
            response.put("message", "Database integrity error: Duplicate or invalid key reference.");
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


    @GetMapping("filter/{category}/{name}/{storeid}")
    public Map<String, Object> getProductName(
            @PathVariable("category") String category,
            @PathVariable("name") String name,
            @PathVariable("storeid") Long storeId) {

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

        // Delete dependencies from the bridge inventory table first
        inventoryRepository.deleteByProductId(id);
        response.put("message", "Successfully deleted product");
        return response;
    }


    @GetMapping("validate/{quantity}/{storeId}/{productId}")
    public boolean validateQuantity(
            @PathVariable("quantity") Integer quantity,
            @PathVariable("storeId") Long storeId,
            @PathVariable("productId") Long productId) {

        Inventory inventory = inventoryRepository.findByProductIdAndStoreId(productId, storeId);

        if (inventory != null && inventory.getStockLevel() >= quantity) {
            return true;
        }
        return false;
    }
// 1. Set Up the Controller Class:
//    - Annotate the class with `@RestController` to indicate that this is a REST controller, which handles HTTP requests and responses.
//    - Use `@RequestMapping("/inventory")` to set the base URL path for all methods in this controller. All endpoints related to inventory will be prefixed with `/inventory`.


// 2. Autowired Dependencies:
//    - Autowire necessary repositories and services:
//      - `ProductRepository` will be used to interact with product data (i.e., finding, updating products).
//      - `InventoryRepository` will handle CRUD operations related to the inventory.
//      - `ServiceClass` will help with the validation logic (e.g., validating product IDs and inventory data).


// 3. Define the `updateInventory` Method:
//    - This method handles HTTP PUT requests to update inventory for a product.
//    - It takes a `CombinedRequest` (containing `Product` and `Inventory`) in the request body.
//    - The product ID is validated, and if valid, the inventory is updated in the database.
//    - If the inventory exists, update it and return a success message. If not, return a message indicating no data available.


// 4. Define the `saveInventory` Method:
//    - This method handles HTTP POST requests to save a new inventory entry.
//    - It accepts an `Inventory` object in the request body.
//    - It first validates whether the inventory already exists. If it exists, it returns a message stating so. If it doesn’t exist, it saves the inventory and returns a success message.


// 5. Define the `getAllProducts` Method:
//    - This method handles HTTP GET requests to retrieve products for a specific store.
//    - It uses the `storeId` as a path variable and fetches the list of products from the database for the given store.
//    - The products are returned in a `Map` with the key `"products"`.


// 6. Define the `getProductName` Method:
//    - This method handles HTTP GET requests to filter products by category and name.
//    - If either the category or name is `"null"`, adjust the filtering logic accordingly.
//    - Return the filtered products in the response with the key `"product"`.


// 7. Define the `searchProduct` Method:
//    - This method handles HTTP GET requests to search for products by name within a specific store.
//    - It uses `name` and `storeId` as parameters and searches for products that match the `name` in the specified store.
//    - The search results are returned in the response with the key `"product"`.


// 8. Define the `removeProduct` Method:
//    - This method handles HTTP DELETE requests to delete a product by its ID.
//    - It first validates if the product exists. If it does, it deletes the product from the `ProductRepository` and also removes the related inventory entry from the `InventoryRepository`.
//    - Returns a success message with the key `"message"` indicating successful deletion.


// 9. Define the `validateQuantity` Method:
//    - This method handles HTTP GET requests to validate if a specified quantity of a product is available in stock for a given store.
//    - It checks the inventory for the product in the specified store and compares it to the requested quantity.
//    - If sufficient stock is available, return `true`; otherwise, return `false`.

}
