package com.edsoft.order_service.service;

import com.edsoft.order_service.model.OrderCreatedEvent;
import com.edsoft.order_service.model.OrderEventEntity;
import com.edsoft.order_service.repository.OrderEventRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaOrderEventConsumer {

    private final OrderEventRepository repository;

    public KafkaOrderEventConsumer(OrderEventRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(topics = "order-created-topic", groupId = "order-event-group")
    public void listen(OrderCreatedEvent event){
        System.out.println("✅ EVENT Listele -> " + event);

        try {
            OrderEventEntity entity = new OrderEventEntity();
            entity.setOrderId(event.getOrderId());
            entity.setRoomNo(event.getRoomNo());
            entity.setPrice(event.getPrice());
            System.out.println("✅ EVENT setter -> " + event);
            repository.save(entity);
            System.out.println("✅ EVENT saved -> " + event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}