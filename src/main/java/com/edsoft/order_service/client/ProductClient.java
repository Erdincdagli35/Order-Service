package com.edsoft.order_service.client;

import com.edsoft.order_service.data.ProductResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
public class ProductClient {

    @Autowired
    private final WebClient webClient;

    private String productServiceUrl = "https://productservice.up.railway.app";

    public ProductClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public ProductResponse getProduct(Long productId) {
        return webClient.get()
                .uri(productServiceUrl + "/api/products/list/" + productId)
                .retrieve()
                .bodyToMono(ProductResponse.class)
                .block();
    }

    public List<ProductResponse> getAllProduct() {
        return webClient.get()
                .uri(productServiceUrl + "/api/products/list")
                .retrieve()
                .bodyToFlux(ProductResponse.class)
                .collectList()
                .block();
    }
}

