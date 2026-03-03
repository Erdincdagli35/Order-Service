package com.edsoft.order_service.service;

import com.edsoft.order_service.model.Bill;
import com.edsoft.order_service.model.Order;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

//    @Autowired
//    JavaMailSender mailSender;
//
//    public void sendOrderMail(Order order, String toMail) throws Exception {
//
//        MimeMessage message = mailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(message, true);
//
//        StringBuilder content = new StringBuilder();
//
//        content.append("<h2>Sipariş Alındı ✅</h2>");
//        content.append("<p><b>Oda No:</b> ").append(order.getRoomNo()).append("</p>");
//        content.append("<p><b>Personel:</b> ").append(order.getPersonalName()).append("</p>");
//        content.append("<hr>");
//        content.append("<h3>Ürünler:</h3>");
//
//        for (Bill bill : order.getBills()) {
//            content.append("<p>")
//                    .append(bill.getProductName())
//                    .append(" - Adet: ")
//                    .append(bill.getPiece())
//                    .append("</p>");
//        }
//
//        content.append("<hr>");
//        content.append("<h3>Toplam Tutar: ")
//                .append(order.getTotal())
//                .append(" ₺</h3>");
//
//        helper.setTo(toMail);
//        helper.setSubject("Siparişiniz Alındı");
//        helper.setText(content.toString(), true);
//        helper.setFrom("edorderflow@gmail.com");
//
//        mailSender.send(message);
//    }
}