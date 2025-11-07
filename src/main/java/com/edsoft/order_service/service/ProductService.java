package com.edsoft.order_service.service;

import com.edsoft.order_service.model.Product;
import com.edsoft.order_service.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    ProductRepository productRepository;

    public List<Product> listAll() {
        return productRepository.findAll();
    }

    public Product listProductById(Long id) {
        return productRepository.findOneById(id);
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public Product update(Long id, Product updatedProduct) {
        Product product = productRepository.findOneById(id);

        product.setName(updatedProduct.getName());
        product.setDescription(updatedProduct.getDescription());
        product.setPrice(updatedProduct.getPrice());
        product.setCategory(updatedProduct.getCategory());

        return productRepository.save(product);
    }

    public void delete(Long id) {
        Product product = productRepository.findOneById(id);
        productRepository.delete(product);
    }
}
