package com.product.productservice.service;

import java.util.List;
import java.util.Optional;

import com.product.productservice.model.Product;

public interface ProductService {

    List<Product> findAll();

    Optional<Product> findById(String id);

    Product create(Product product);

    Product update(String id, Product product);

    boolean deleteById(String id);
}
