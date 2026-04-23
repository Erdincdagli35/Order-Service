package com.edsoft.order_service.service;

import com.edsoft.order_service.client.ProductClient;
import com.edsoft.order_service.client.RoomClient;
import com.edsoft.order_service.data.OrderByRoomResponse;
import com.edsoft.order_service.data.OrderCreateRequest;
import com.edsoft.order_service.data.ProductResponse;
import com.edsoft.order_service.data.RoomResponse;
import com.edsoft.order_service.model.Bill;
import com.edsoft.order_service.model.Order;
import com.edsoft.order_service.repository.OrderRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class OrderService {

    @Autowired
    private final ProductClient productClient;

    @Autowired
    private final RoomClient roomClient;

    @Autowired
    private final OrderRepository orderRepository;

    // Aldığımız bilgiler (Güvenlik için bunları config dosyasından çekmek daha iyidir)
    private static final String TELEGRAM_TOKEN = "8793995144:AAHdC4lPxuuVplXbqpQcYAAdbY5fV7KC87s";
    private static final String CHAT_ID = "8416899324";

    public OrderService(ProductClient productClient, RoomClient roomClient,
                        OrderRepository orderRepository) {
        this.productClient = productClient;
        this.roomClient = roomClient;
        this.orderRepository = orderRepository;
    }

    @Async
    // Telegram'a istek atan yardımcı metod
    private void sendTelegramNotification(String messageText) {
        try {
            String encodedMessage = URLEncoder.encode(messageText, StandardCharsets.UTF_8.toString());

            String urlString = String.format(
                    "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s",
                    TELEGRAM_TOKEN, CHAT_ID, encodedMessage
            );

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            System.out.println("Telegram response code: " + responseCode);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            responseCode >= 200 && responseCode < 300
                                    ? conn.getInputStream()
                                    : conn.getErrorStream()
                    )
            );

            String line;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            System.out.println("Telegram response body: " + response);

            conn.disconnect();

        } catch (Exception e) {
            e.printStackTrace(); // 🔥 BUNU DEĞİŞTİR (çok önemli)
        }
    }


    public Order createOrder(OrderCreateRequest req) throws Exception {

        if (req == null || req.getItems() == null || req.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        // 1) Ürünleri çek
        List<Long> productIds = req.getItems()
                .stream()
                .map(OrderCreateRequest.OrderItemRequest::getProductId)
                .toList();

        Map<Long, ProductResponse> productMap = new HashMap<>();
        for (Long productId : productIds) {
            ProductResponse product = productClient.getProduct(productId); // handle exception upstream if not found
            if (product == null) {
                throw new Exception("Product not found: " + productId);
            }
            productMap.put(productId, product);
        }

        List<Bill> bills = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderCreateRequest.OrderItemRequest itemReq : req.getItems()) {
            Long pId = itemReq.getProductId();
            Integer qty = itemReq.getQty() == null ? 0 : itemReq.getQty();

            if (qty <= 0) {
                throw new IllegalArgumentException("Quantity must be > 0 for product: " + pId);
            }

            ProductResponse product = productMap.get(pId);
            if (product == null) {
                throw new Exception("Product not found: " + pId);
            }

            BigDecimal unitPrice = product.getPrice() == null ? BigDecimal.ZERO : product.getPrice();
            BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(qty));

            total = total.add(itemTotal);

            Bill bill = new Bill();
            bill.setProductName(product.getName());
            bill.setPiece(qty);
            bill.setPersonalName(req.getPersonalName());
            bills.add(bill);
        }

        // 4) Order oluştur ve kaydet
        Order order = new Order();
        order.setStatus("Pending");
        order.setTotal(total.setScale(2, RoundingMode.HALF_UP));
        order.setBills(bills);
        order.setPersonalName(req.getPersonalName());
        order.setRoomNo(req.getRoomNo());

        Order savedOrder = orderRepository.save(order);

        String mesaj = "🚀 Yeni Sipariş Öğesi Eklendi!\n" +
                "Oda No: " + (savedOrder.getRoomNo()) + "\n" +
                "Fiyat: " + savedOrder.getTotal() + "\n" +
                "Sipariş Durumu : " + savedOrder.getStatus() + "\n"    ;


        sendTelegramNotification(mesaj);


        return savedOrder;
    }


    public List<Order> listAllOrders() {
        return orderRepository.findAllByOrderByIdDesc();
    }

    public List<Order> listAllOrders(String roomNo) {
        if (roomNo != null && !roomNo.isBlank()) {
            return orderRepository.findAllByRoomNoOrderByIdDesc(roomNo);
        }

        return orderRepository.findAllByOrderByIdDesc();
    }

    public @Nullable Order listOrder(Long id) {
        return orderRepository.findOneById(id);
    }

    public void cancelOrder(Long id) {
        Order order = orderRepository.findOneById(id);
        orderRepository.delete(order);
    }

    public Order willDeliver(Long id) {
        Order order = orderRepository.findOneById(id);
        order.setStatus("Yolda");
        return orderRepository.save(order);
    }

    public Order delivered(Long id) {
        Order order = orderRepository.findOneById(id);
        order.setStatus("Teslim edildi");
        return orderRepository.save(order);
    }

    public List<OrderByRoomResponse> listOrderByRoomNo() {
        List<RoomResponse> roomResponses = roomClient.getAllRoom();
        if (roomResponses == null || roomResponses.isEmpty()) {
            return Collections.emptyList();
        }

        List<Order> allOrders = listAllOrders();
        Map<String, Stats> statsMap = new HashMap<>();

        if (allOrders != null && !allOrders.isEmpty()) {
            for (Order order : allOrders) {
                if (order == null) continue;
                String rNo = order.getRoomNo();
                if (rNo == null) continue;

                Stats s = statsMap.computeIfAbsent(rNo, k -> new Stats());
                s.incrementOrderCount();

                // Buradaki önemli değişiklik: artık order.getTotal() kullanıyoruz
                BigDecimal orderTotal = order.getTotal() == null ? BigDecimal.ZERO : order.getTotal();
                s.addRevenue(orderTotal);
            }
        }

        List<OrderByRoomResponse> result = new ArrayList<>(roomResponses.size());
        for (RoomResponse room : roomResponses) {
            if (room == null) continue;
            String rNo = room.getRoomNo();
            Stats s = (rNo == null) ? null : statsMap.get(rNo);

            int orderCount = (s == null) ? 0 : s.getOrderCount();
            BigDecimal totalRevenue = (s == null) ? BigDecimal.ZERO : s.getTotalRevenue();

            OrderByRoomResponse resp = buildOrderByRoomResponse(room, orderCount, totalRevenue);
            result.add(resp);
        }

        // opsiyonel: odaları roomNo'ya göre sırala
        result.sort(Comparator.comparing(OrderByRoomResponse::getRoomNo, Comparator.nullsLast(String::compareTo)));

        return result;
    }

    public List<Order> allOrdersWillDeliver() {
        List<Order> allOrders = listAllOrders();

        List<Order> allOrdersByCustomer = new ArrayList<>();
        for (Order order : allOrders) {
            if (order.getStatus().equals("Yolda") || order.getStatus().equals("Hazırlanıyor")) {
                allOrdersByCustomer.add(order);
            }
        }

        return allOrdersByCustomer;
    }

    public List<Order> allOrdersWillDeliverCustomer(String roomNo) {
        List<Order> allOrders = listAllOrders();

        List<Order> allOrdersByCustomer = new ArrayList<>();
        for (Order order : allOrders) {
            if (roomNo.equals(order.getRoomNo()) && order.getStatus().equals("Yolda")) {
                allOrdersByCustomer.add(order);
            }
        }

        return allOrdersByCustomer;
    }

    /**
     * Basit aggregation taşıyıcı sınıfı
     */
    private static class Stats {
        private int orderCount = 0;
        private BigDecimal totalRevenue = BigDecimal.ZERO;

        void incrementOrderCount() {
            orderCount++;
        }

        void addRevenue(BigDecimal amount) {
            if (amount != null) {
                totalRevenue = totalRevenue.add(amount);
            }
        }

        int getOrderCount() {
            return orderCount;
        }

        BigDecimal getTotalRevenue() {
            return totalRevenue;
        }
    }

    private OrderByRoomResponse buildOrderByRoomResponse(RoomResponse room, int orderCount, BigDecimal totalRevenue) {
        OrderByRoomResponse r = new OrderByRoomResponse();
        r.setRoomNo(room.getRoomNo());
        r.setDescription(room.getDescription());
        r.setOrderCount(orderCount);
        r.setTotalRevenue(totalRevenue == null ? BigDecimal.ZERO : totalRevenue);
        return r;
    }
}
