package com.product.productservice.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.product.productservice.model.Product;
import com.product.productservice.service.ProductService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/almacen")
@RequiredArgsConstructor
@Slf4j
public class AlmacenController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product createFromRetry(@RequestBody Map<String, Object> payload) {
        @SuppressWarnings("unchecked")
        Map<String, Object> data = payload.get("data") instanceof Map<?, ?>
                ? (Map<String, Object>) payload.get("data")
                : payload;

        Product product = Product.builder()
                .nombre((String) data.get("name"))
                .descripcion((String) data.get("description"))
                .precio(data.get("price") != null ? ((Number) data.get("price")).doubleValue() : null)
                .build();

        Product saved = productService.create(product);
        log.info("[Almacen] Product created from retry – id={}, nombre={}", saved.getId(), saved.getNombre());
        return saved;
    }
}
