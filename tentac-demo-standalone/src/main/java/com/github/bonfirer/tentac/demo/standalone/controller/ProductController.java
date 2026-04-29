package com.github.bonfirer.tentac.demo.standalone.controller;

import com.github.bonfirer.tentac.annotation.*;

import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Product controller using ONLY @MarkdownApi* annotations (no Swagger2).
 * Proves the library works standalone without swagger-annotations on the classpath.
 */
@RestController
@RequestMapping("/api/products")
@MarkdownApi(tags = "Product Catalog", description = "Operations for managing products")
public class ProductController {

    /**
     * List all products.
     */
    @GetMapping
    @MarkdownApiOperation(value = "List all products",
                          notes = "Returns all products in the catalog")
    public List<Product> getAllProducts() {
        return List.of(new Product("p1", "Widget", 9.99, true));
    }

    /**
     * Get a product by SKU.
     */
    @GetMapping("/{sku}")
    @MarkdownApiOperation(value = "Get product by SKU",
                          notes = "Finds a single product by its unique SKU code")
    public Product getProduct(
            @PathVariable("sku")
            @MarkdownApiParam(name = "sku", value = "Product SKU code", required = true,
                              example = "p1", in = "path")
            String sku) {
        return new Product(sku, "Widget", 9.99, true);
    }

    /**
     * Create a new product.
     */
    @PostMapping
    @MarkdownApiOperation(value = "Create product",
                          notes = "Adds a new product to the catalog")
    public Product createProduct(
            @RequestBody
            @MarkdownApiParam(value = "Product definition", required = true)
            Product product) {
        return product;
    }

    /**
     * Update a product.
     */
    @PutMapping("/{sku}")
    @MarkdownApiOperation(value = "Update product",
                          notes = "Updates an existing product in the catalog")
    public Product updateProduct(
            @PathVariable("sku")
            @MarkdownApiParam(name = "sku", value = "Product SKU to update", required = true,
                              example = "p1", in = "path")
            String sku,
            @RequestBody
            @MarkdownApiParam(value = "Updated product data", required = true)
            Product product) {
        return product;
    }

    /**
     * Delete a product.
     */
    @DeleteMapping("/{sku}")
    @MarkdownApiOperation(value = "Delete product",
                          notes = "Removes a product from the catalog")
    public void deleteProduct(
            @PathVariable("sku")
            @MarkdownApiParam(name = "sku", value = "Product SKU to delete", required = true)
            String sku) {
    }

    /**
     * Search products with filters.
     */
    @GetMapping("/search")
    @MarkdownApiOperation(value = "Search products",
                          notes = "Search products by name or category")
    public List<Product> searchProducts(
            @RequestParam("q")
            @MarkdownApiParam(name = "q", value = "Search query (name or keyword)",
                              required = true, example = "widget")
            String query,
            @RequestParam(value = "category", defaultValue = "all")
            @MarkdownApiParam(name = "category", value = "Product category filter",
                              defaultValue = "all")
            String category,
            @RequestParam(value = "maxPrice", defaultValue = "9999")
            @MarkdownApiParam(name = "maxPrice", value = "Maximum price filter",
                              defaultValue = "9999")
            double maxPrice) {
        return List.of(new Product("p1", "Widget", 9.99, true));
    }

    /**
     * Product model with standalone field descriptions.
     */
    public static class Product {
        @MarkdownApiModelProperty(value = "Product SKU code", example = "p1")
        private String sku;

        @MarkdownApiModelProperty(value = "Product display name", required = true,
                                  example = "Super Widget")
        private String name;

        @MarkdownApiModelProperty(value = "Product price in USD", example = "9.99")
        private double price;

        @MarkdownApiModelProperty(value = "Whether the product is currently in stock",
                                  example = "true")
        private boolean inStock;

        public Product() {}

        public Product(String sku, String name, double price, boolean inStock) {
            this.sku = sku;
            this.name = name;
            this.price = price;
            this.inStock = inStock;
        }

        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        public boolean isInStock() { return inStock; }
        public void setInStock(boolean inStock) { this.inStock = inStock; }
    }
}
