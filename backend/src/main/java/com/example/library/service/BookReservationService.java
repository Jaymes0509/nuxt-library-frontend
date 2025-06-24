package com.example.library.service;

import com.example.library.entity.BookReservation;
import com.example.library.entity.Book;
import com.example.library.entity.Member;
import com.example.library.repository.BookReservationRepository;
import com.example.library.repository.BookRepository;
import com.example.library.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookReservationService {

    private static final Logger logger = LoggerFactory.getLogger(BookReservationService.class);

    @Autowired
    private BookReservationRepository reservationRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MemberRepository memberRepository;

    /**
     * 獲取用戶的預約歷史記錄
     */
    public List<Map<String, Object>> getReservationHistory(Long userId, boolean includeCancelled) {
        try {
            List<BookReservation> reservations;
            
            if (userId != null) {
                // 根據用戶ID過濾預約記錄
                if (includeCancelled) {
                    reservations = reservationRepository.findByUserId(userId);
                } else {
                    reservations = reservationRepository.findByUserIdAndStatusNot(userId, "cancelled");
                }
            } else {
                // 如果沒有指定用戶ID，返回所有記錄（管理員功能）
                if (includeCancelled) {
                    reservations = reservationRepository.findAll();
                } else {
                    reservations = reservationRepository.findByStatusNot("cancelled");
                }
            }

            return reservations.stream().map(this::convertToMap).collect(Collectors.toList());
            
        } catch (Exception e) {
            logger.error("獲取預約歷史記錄失敗", e);
            throw new RuntimeException("獲取預約歷史記錄失敗", e);
        }
    }

    /**
     * 創建單本預約
     */
    public BookReservation createReservation(Long userId, Long bookId, String pickupLocation, String pickupMethod) {
        try {
            // 檢查用戶是否存在
            Optional<Member> memberOpt = memberRepository.findById(userId);
            if (!memberOpt.isPresent()) {
                throw new RuntimeException("用戶不存在，userId: " + userId);
            }

            // 檢查書籍是否存在
            Optional<Book> bookOpt = bookRepository.findById(bookId);
            if (!bookOpt.isPresent()) {
                throw new RuntimeException("書籍不存在，bookId: " + bookId);
            }

            // 檢查是否已經預約過同一本書
            List<BookReservation> existingReservations = reservationRepository.findByUserIdAndBookIdAndStatusNot(
                userId, bookId, "cancelled");
            if (!existingReservations.isEmpty()) {
                throw new RuntimeException("您已經預約過這本書");
            }

            // 創建預約記錄
            BookReservation reservation = new BookReservation();
            reservation.setUserId(userId);
            reservation.setBookId(bookId);
            reservation.setReserveTime(LocalDateTime.now());
            reservation.setPickupLocation(pickupLocation != null ? pickupLocation : "一樓服務台");
            reservation.setPickupMethod(pickupMethod != null ? pickupMethod : "親自取書");
            reservation.setStatus("pending");
            reservation.setReserveStatus(1);
            reservation.setCreatedAt(LocalDateTime.now());
            reservation.setUpdatedAt(LocalDateTime.now());

            return reservationRepository.save(reservation);
            
        } catch (Exception e) {
            logger.error("創建預約失敗", e);
            throw new RuntimeException("創建預約失敗", e);
        }
    }

    /**
     * 批量創建預約
     */
    public List<Map<String, Object>> batchCreateReservations(Long userId, List<Map<String, Object>> books, 
                                                           String pickupLocation, String pickupMethod) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        for (Map<String, Object> bookData : books) {
            Map<String, Object> result = new HashMap<>();
            
            try {
                Long bookId = Long.valueOf(bookData.get("bookId").toString());
                
                BookReservation reservation = createReservation(userId, bookId, pickupLocation, pickupMethod);
                
                result.put("status", "success");
                result.put("bookId", bookId);
                result.put("reservationId", reservation.getId());
                result.put("message", "預約成功");
                
            } catch (Exception e) {
                Long bookId = Long.valueOf(bookData.get("bookId").toString());
                result.put("status", "fail");
                result.put("bookId", bookId);
                result.put("message", e.getMessage());
                logger.error("批量預約失敗，bookId: {}, error: {}", bookId, e.getMessage());
            }
            
            results.add(result);
        }
        
        return results;
    }

    /**
     * 取消預約
     */
    public void cancelReservation(Long reservationId) {
        try {
            Optional<BookReservation> reservationOpt = reservationRepository.findById(reservationId);
            if (!reservationOpt.isPresent()) {
                throw new RuntimeException("預約記錄不存在，reservationId: " + reservationId);
            }

            BookReservation reservation = reservationOpt.get();
            reservation.setStatus("cancelled");
            reservation.setUpdatedAt(LocalDateTime.now());
            
            reservationRepository.save(reservation);
            
        } catch (Exception e) {
            logger.error("取消預約失敗", e);
            throw new RuntimeException("取消預約失敗", e);
        }
    }

    /**
     * 批量取消預約
     */
    public void batchCancelReservations(List<Long> reservationIds) {
        try {
            for (Long reservationId : reservationIds) {
                cancelReservation(reservationId);
            }
        } catch (Exception e) {
            logger.error("批量取消預約失敗", e);
            throw new RuntimeException("批量取消預約失敗", e);
        }
    }

    /**
     * 更新預約狀態
     */
    public void updateReservationStatus(Long reservationId, String status) {
        try {
            Optional<BookReservation> reservationOpt = reservationRepository.findById(reservationId);
            if (!reservationOpt.isPresent()) {
                throw new RuntimeException("預約記錄不存在，reservationId: " + reservationId);
            }

            BookReservation reservation = reservationOpt.get();
            reservation.setStatus(status);
            reservation.setUpdatedAt(LocalDateTime.now());
            
            reservationRepository.save(reservation);
            
        } catch (Exception e) {
            logger.error("更新預約狀態失敗", e);
            throw new RuntimeException("更新預約狀態失敗", e);
        }
    }

    /**
     * 將預約實體轉換為 Map
     */
    private Map<String, Object> convertToMap(BookReservation reservation) {
        Map<String, Object> map = new HashMap<>();
        map.put("reservation_id", reservation.getId());
        map.put("user_id", reservation.getUserId());
        map.put("book_id", reservation.getBookId());
        map.put("reserve_time", reservation.getReserveTime());
        map.put("pickup_location", reservation.getPickupLocation());
        map.put("pickup_method", reservation.getPickupMethod());
        map.put("status", reservation.getStatus());
        map.put("created_at", reservation.getCreatedAt());
        map.put("updated_at", reservation.getUpdatedAt());
        
        // 獲取書籍資訊
        Optional<Book> bookOpt = bookRepository.findById(reservation.getBookId());
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            map.put("book_title", book.getTitle());
            map.put("book_author", book.getAuthor());
            map.put("book_isbn", book.getIsbn());
            map.put("book_publisher", book.getPublisher());
            map.put("classification", book.getClassification());
        }
        
        return map;
    }
} 