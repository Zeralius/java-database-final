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
    public boolean validateInventory(Inventory inventory) {
        if (inventory == null || inventory.getProduct() == null || inventory.getStore() == null) {
            return true;
        }

        // FIXED: Invoking repository lookup explicitly using structural child values.
        Inventory existingInventory = inventoryRepository.findByProductIdAndStoreId(
                inventory.getProduct().getId(),
                inventory.getStore().getId()
        );

        return existingInventory == null;
    }

    public boolean validateProduct(Product product) {
        if (product == null || product.getName() == null) {
            return true;
        }

        Product existingProduct = productRepository.findByName(product.getName());
        return existingProduct == null;
    }

    public boolean validateProductId(long id) {
        Product product = productRepository.findById(id).orElse(null);
        return product != null;
    }

    public Inventory getInventoryId(Inventory inventory) {
        if (inventory == null || inventory.getProduct() == null || inventory.getStore() == null) {
            return null;
        }

        // FIXED: Explicitly querying the database using both values and returning the full database record row
        return inventoryRepository.findByProductIdAndStoreId(
                inventory.getProduct().getId(),
                inventory.getStore().getId()
        );
    }

}
