package com.edsoft.order_service.data;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RoomResponse {
    private String id;
    private String roomNo;
    private String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
