package com.example.library.controller;

import com.example.library.entity.BookReservation;
import com.example.library.service.BookReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookreservations")
@CrossOrigin(origins = "*")
public class BookReservationController {

    private static final Logger logger = LoggerFactory.getLogger(BookReservationController.class);

    @Autowired
    private BookReservationService reservationService;

    /**
     * 獲取用戶的預約歷史記錄
     */
    @GetMapping("/history")
    public ResponseEntity<List<Map<String, Object>>> getReservationHistory(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "false") boolean includeCancelled) {
        
        try {
            logger.info("獲取預約歷史記錄，userId: {}, includeCancelled: {}", userId, includeCancelled);
            
            List<Map<String, Object>> reservations = reservationService.getReservationHistory(userId, includeCancelled);
            
            return ResponseEntity.ok(reservations);
            
        } catch (Exception e) {
            logger.error("獲取預約歷史記錄失敗", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 創建單本預約
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createReservation(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            Long bookId = Long.valueOf(request.get("bookId").toString());
            String pickupLocation = (String) request.get("pickupLocation");
            String pickupMethod = (String) request.get("pickupMethod");
            
            logger.info("創建預約，userId: {}, bookId: {}", userId, bookId);
            
            BookReservation reservation = reservationService.createReservation(userId, bookId, pickupLocation, pickupMethod);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "reservationId", reservation.getId(),
                "message", "預約創建成功"
            ));
            
        } catch (Exception e) {
            logger.error("創建預約失敗", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "創建預約失敗：" + e.getMessage()
            ));
        }
    }

    /**
     * 批量創建預約
     */
    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> batchReservation(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            List<Map<String, Object>> books = (List<Map<String, Object>>) request.get("books");
            String pickupLocation = (String) request.get("pickupLocation");
            String pickupMethod = (String) request.get("pickupMethod");
            
            logger.info("批量創建預約，userId: {}, books count: {}", userId, books.size());
            
            List<Map<String, Object>> results = reservationService.batchCreateReservations(
                userId, books, pickupLocation, pickupMethod);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "results", results,
                "message", "批量預約創建成功"
            ));
            
        } catch (Exception e) {
            logger.error("批量創建預約失敗", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "批量創建預約失敗：" + e.getMessage()
            ));
        }
    }

    /**
     * 取消預約
     */
    @PutMapping("/{reservationId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelReservation(@PathVariable Long reservationId) {
        try {
            logger.info("取消預約，reservationId: {}", reservationId);
            
            reservationService.cancelReservation(reservationId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "預約取消成功"
            ));
            
        } catch (Exception e) {
            logger.error("取消預約失敗", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "取消預約失敗：" + e.getMessage()
            ));
        }
    }

    /**
     * 批量取消預約
     */
    @PutMapping("/batch-cancel")
    public ResponseEntity<Map<String, Object>> batchCancelReservations(@RequestBody Map<String, Object> request) {
        try {
            List<Long> reservationIds = (List<Long>) request.get("reservationIds");
            
            logger.info("批量取消預約，reservationIds: {}", reservationIds);
            
            reservationService.batchCancelReservations(reservationIds);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "批量取消預約成功"
            ));
            
        } catch (Exception e) {
            logger.error("批量取消預約失敗", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "批量取消預約失敗：" + e.getMessage()
            ));
        }
    }

    /**
     * 更新預約狀態
     */
    @PutMapping("/{reservationId}/status")
    public ResponseEntity<Map<String, Object>> updateReservationStatus(
            @PathVariable Long reservationId,
            @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");
            
            logger.info("更新預約狀態，reservationId: {}, status: {}", reservationId, status);
            
            reservationService.updateReservationStatus(reservationId, status);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "預約狀態更新成功"
            ));
            
        } catch (Exception e) {
            logger.error("更新預約狀態失敗", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "更新預約狀態失敗：" + e.getMessage()
            ));
        }
    }
} 