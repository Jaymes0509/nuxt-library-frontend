package com.example.library.controller;

import com.example.library.service.ReservationNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservation-notification")
@CrossOrigin(origins = "*")
public class ReservationNotificationController {

    private static final Logger logger = LoggerFactory.getLogger(ReservationNotificationController.class);

    @Autowired
    private ReservationNotificationService notificationService;

    /**
     * 發送預約成功通知郵件
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendNotificationEmail(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            List<String> reservationIdsStr = (List<String>) request.get("reservationIds");
            List<Long> reservationIds = reservationIdsStr.stream()
                .map(Long::valueOf)
                .toList();

            logger.info("收到郵件通知請求，userId: {}, reservationIds: {}", userId, reservationIds);

            if (reservationIds.size() == 1) {
                // 單本預約
                notificationService.sendSingleReservationNotification(userId, reservationIds.get(0));
            } else {
                // 批量預約
                notificationService.sendBatchReservationNotification(userId, reservationIds);
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "郵件發送成功"
            ));

        } catch (Exception e) {
            logger.error("發送郵件通知失敗", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "郵件發送失敗：" + e.getMessage()
            ));
        }
    }

    /**
     * 測試郵件發送功能
     */
    @PostMapping("/test-email")
    public ResponseEntity<Map<String, Object>> testEmail(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            Long reservationId = Long.valueOf(request.get("reservationId").toString());

            logger.info("收到測試郵件請求，userId: {}, reservationId: {}", userId, reservationId);

            notificationService.sendSingleReservationNotification(userId, reservationId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "測試郵件發送成功"
            ));

        } catch (Exception e) {
            logger.error("發送測試郵件失敗", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "測試郵件發送失敗：" + e.getMessage()
            ));
        }
    }
} 