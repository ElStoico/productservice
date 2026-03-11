package com.product.productservice.service.impl;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.product.productservice.model.Product;
import com.product.productservice.repository.ProductRepository;
import com.product.productservice.service.ProductService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public List<Product> findAll() {
        log.debug("Consultando todos los productos");
        return productRepository.findAll();
    }

    @Override
    public Optional<Product> findById(String id) {
        log.debug("Buscando producto por id={}", id);
        return productRepository.findById(id);
    }

    @Override
    public Product create(Product product) {
        log.info("Creando producto nombre={} precio={}", product.getNombre(), product.getPrecio());
        product.setId(null);
        return productRepository.save(product);
    }

    @Override
    public Product update(String id, Product product) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Producto no encontrado con id: " + id));

        existingProduct.setNombre(product.getNombre());
        existingProduct.setDescripcion(product.getDescripcion());
        existingProduct.setPrecio(product.getPrecio());

        log.info("Actualizando producto id={}", id);
        return productRepository.save(existingProduct);
    }

    @Override
    public boolean deleteById(String id) {
        if (!productRepository.existsById(id)) {
            return false;
        }

        log.info("Eliminando producto id={}", id);
        productRepository.deleteById(id);
        return true;
    }
}
