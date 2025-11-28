package com.edsoft.order_service.client;

import com.edsoft.order_service.data.ProductResponse;
import com.edsoft.order_service.data.RoomResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
public class RoomClient {

    @Autowired
    private final WebClient webClient;

    private String roomServiceUrl = "https://roomservice.up.railway.app";

    public RoomClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public List<RoomResponse> getAllRoom() {
        return webClient.get()
                .uri(roomServiceUrl + "/rooms/list/all")
                .retrieve()
                .bodyToFlux(RoomResponse.class)
                .collectList()
                .block();
    }
}
