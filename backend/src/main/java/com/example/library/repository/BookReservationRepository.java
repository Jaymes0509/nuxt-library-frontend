package com.example.library.repository;

import com.example.library.entity.BookReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookReservationRepository extends JpaRepository<BookReservation, Long> {

    /**
     * 根據用戶ID查找預約記錄
     */
    List<BookReservation> findByUserId(Long userId);

    /**
     * 根據用戶ID和狀態查找預約記錄
     */
    List<BookReservation> findByUserIdAndStatus(Long userId, String status);

    /**
     * 根據用戶ID和狀態（排除指定狀態）查找預約記錄
     */
    List<BookReservation> findByUserIdAndStatusNot(Long userId, String status);

    /**
     * 根據狀態查找預約記錄
     */
    List<BookReservation> findByStatus(String status);

    /**
     * 根據狀態（排除指定狀態）查找預約記錄
     */
    List<BookReservation> findByStatusNot(String status);

    /**
     * 根據用戶ID和書籍ID查找預約記錄
     */
    List<BookReservation> findByUserIdAndBookId(Long userId, Long bookId);

    /**
     * 根據用戶ID、書籍ID和狀態（排除指定狀態）查找預約記錄
     */
    List<BookReservation> findByUserIdAndBookIdAndStatusNot(Long userId, Long bookId, String status);

    /**
     * 根據書籍ID查找預約記錄
     */
    List<BookReservation> findByBookId(Long bookId);

    /**
     * 查找有效的預約記錄（狀態不是 cancelled）
     */
    @Query("SELECT r FROM BookReservation r WHERE r.status != 'cancelled'")
    List<BookReservation> findActiveReservations();

    /**
     * 根據用戶ID查找有效的預約記錄
     */
    @Query("SELECT r FROM BookReservation r WHERE r.userId = :userId AND r.status != 'cancelled'")
    List<BookReservation> findActiveReservationsByUserId(@Param("userId") Long userId);

    /**
     * 統計用戶的有效預約數量
     */
    @Query("SELECT COUNT(r) FROM BookReservation r WHERE r.userId = :userId AND r.status != 'cancelled'")
    long countActiveReservationsByUserId(@Param("userId") Long userId);
} 