package com.project.code.Service;


import com.project.code.Model.Inventory;
import com.project.code.Model.Product;
import com.project.code.Repo.InventoryRepository;
import com.project.code.Repo.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class ServiceClass {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    public ServiceClass(ProductRepository productRepository, InventoryRepository inventoryRepository) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * Checks whether an inventory record already exists for a given product and store combination.
     * Returns false if it already exists (preventing duplicate entry), otherwise true.
     */
    public boolean validateInventory(Inventory inventory) {
        // Prevent NullPointerExceptions if the inventory object or its components are null
        if (inventory == null || inventory.getProduct() == null || inventory.getStore() == null) {
            return true;
        }

        Inventory existingInventory = inventoryRepository.findByProductIdAndStoreId(
                inventory.getProduct().getId(),
                inventory.getStore().getId()
        );

        // If it exists, return false. Otherwise, return true.
        return existingInventory == null;
    }

    /**
     * Checks whether a product already exists by its name.
     * Returns false if a product with the same name exists, otherwise true.
     */
    public boolean validateProduct(Product product) {
        if (product == null || product.getName() == null) {
            return true;
        }

        Product existingProduct = productRepository.findByName(product.getName());

        // If a product with that name exists, return false. Otherwise, return true.
        return existingProduct == null;
    }

    /**
     * Validates whether a product exists by its ID.
     * Returns false if the product does not exist, otherwise true.
     */
    public boolean validateProductId(long id) {
        Product product = productRepository.findById(id);

        // If the product is null, it doesn't exist (return false). Otherwise, return true.
        return product != null;
    }

    /**
     * Fetches the unique inventory record for a given product and store combination.
     */
    public Inventory getInventoryId(Inventory inventory) {
        if (inventory == null || inventory.getProduct() == null || inventory.getStore() == null) {
            return null;
        }

        return inventoryRepository.findByProductIdAndStoreId(
                inventory.getProduct().getId(),
                inventory.getStore().getId()
        );
    }
    
// 1. **validateInventory Method**:
//    - Checks if an inventory record exists for a given product and store combination.
//    - Parameters: `Inventory inventory`
//    - Return Type: `boolean` (Returns `false` if inventory exists, otherwise `true`)

// 2. **validateProduct Method**:
//    - Checks if a product exists by its name.
//    - Parameters: `Product product`
//    - Return Type: `boolean` (Returns `false` if a product with the same name exists, otherwise `true`)

// 3. **ValidateProductId Method**:
//    - Checks if a product exists by its ID.
//    - Parameters: `long id`
//    - Return Type: `boolean` (Returns `false` if the product does not exist with the given ID, otherwise `true`)

// 4. **getInventoryId Method**:
//    - Fetches the inventory record for a given product and store combination.
//    - Parameters: `Inventory inventory`
//    - Return Type: `Inventory` (Returns the inventory record for the product-store combination)

}
