package com.example.library.service;

import com.example.library.entity.Member;
import com.example.library.entity.Book;
import com.example.library.entity.BookReservation;
import com.example.library.repository.MemberRepository;
import com.example.library.repository.BookRepository;
import com.example.library.repository.BookReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class ReservationNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(ReservationNotificationService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookReservationRepository reservationRepository;

    /**
     * 發送單本預約成功通知郵件
     */
    public void sendSingleReservationNotification(Long userId, Long reservationId) {
        try {
            // 獲取會員資訊
            Optional<Member> memberOpt = memberRepository.findById(userId);
            if (!memberOpt.isPresent()) {
                logger.error("找不到會員資訊，userId: {}", userId);
                return;
            }
            Member member = memberOpt.get();

            // 獲取預約資訊
            Optional<BookReservation> reservationOpt = reservationRepository.findById(reservationId);
            if (!reservationOpt.isPresent()) {
                logger.error("找不到預約資訊，reservationId: {}", reservationId);
                return;
            }
            BookReservation reservation = reservationOpt.get();

            // 獲取書籍資訊
            Optional<Book> bookOpt = bookRepository.findById(reservation.getBookId());
            if (!bookOpt.isPresent()) {
                logger.error("找不到書籍資訊，bookId: {}", reservation.getBookId());
                return;
            }
            Book book = bookOpt.get();

            // 生成郵件內容
            String emailContent = generateSingleReservationEmailContent(member, book, reservation);

            // 發送郵件
            sendEmail(member.getEmail(), "圖書館預約成功通知", emailContent);

            logger.info("單本預約通知郵件發送成功，userId: {}, reservationId: {}", userId, reservationId);

        } catch (Exception e) {
            logger.error("發送單本預約通知郵件失敗，userId: {}, reservationId: {}", userId, reservationId, e);
        }
    }

    /**
     * 發送批量預約成功通知郵件
     */
    public void sendBatchReservationNotification(Long userId, List<Long> reservationIds) {
        try {
            // 獲取會員資訊
            Optional<Member> memberOpt = memberRepository.findById(userId);
            if (!memberOpt.isPresent()) {
                logger.error("找不到會員資訊，userId: {}", userId);
                return;
            }
            Member member = memberOpt.get();

            // 獲取所有預約和書籍資訊
            List<BookReservation> reservations = reservationRepository.findAllById(reservationIds);
            List<Book> books = bookRepository.findAllById(
                reservations.stream().map(BookReservation::getBookId).toList()
            );

            // 生成郵件內容
            String emailContent = generateBatchReservationEmailContent(member, books, reservations);

            // 發送郵件
            sendEmail(member.getEmail(), "圖書館批量預約成功通知", emailContent);

            logger.info("批量預約通知郵件發送成功，userId: {}, reservationIds: {}", userId, reservationIds);

        } catch (Exception e) {
            logger.error("發送批量預約通知郵件失敗，userId: {}, reservationIds: {}", userId, reservationIds, e);
        }
    }

    /**
     * 生成單本預約郵件內容
     */
    private String generateSingleReservationEmailContent(Member member, Book book, BookReservation reservation) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm");
        String reservationTime = reservation.getReserveTime().format(formatter);
        String expiryTime = reservation.getReserveTime().plusDays(7).format(formatter);

        return String.format("""
            親愛的 %s 您好，

            您的圖書預約已成功建立！

            【預約詳細資訊】
            預約編號：%d
            書籍名稱：%s
            作者：%s
            ISBN：%s
            預約時間：%s
            取書期限：%s
            取書地點：%s
            取書方式：%s

            【重要提醒】
            1. 請在取書期限內到館取書，逾期將自動取消預約
            2. 取書時請攜帶有效證件
            3. 如有任何問題，請聯繫圖書館服務台

            感謝您使用圖書館服務！

            此為系統自動發送，請勿回覆此郵件。
            ---
            Dear %s,

            Your book reservation has been successfully created!

            【Reservation Details】
            Reservation ID: %d
            Book Title: %s
            Author: %s
            ISBN: %s
            Reservation Time: %s
            Pickup Deadline: %s
            Pickup Location: %s
            Pickup Method: %s

            【Important Reminders】
            1. Please pick up your book before the deadline
            2. Bring valid ID when picking up
            3. Contact the library service desk for any questions

            Thank you for using our library services!

            This is an automated message, please do not reply.
            """,
            member.getName(), reservation.getId(), book.getTitle(), book.getAuthor(), book.getIsbn(),
            reservationTime, expiryTime, reservation.getPickupLocation(), reservation.getPickupMethod(),
            member.getName(), reservation.getId(), book.getTitle(), book.getAuthor(), book.getIsbn(),
            reservationTime, expiryTime, reservation.getPickupLocation(), reservation.getPickupMethod()
        );
    }

    /**
     * 生成批量預約郵件內容
     */
    private String generateBatchReservationEmailContent(Member member, List<Book> books, List<BookReservation> reservations) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm");
        String reservationTime = reservations.get(0).getReserveTime().format(formatter);
        String expiryTime = reservations.get(0).getReserveTime().plusDays(7).format(formatter);

        StringBuilder booksList = new StringBuilder();
        for (int i = 0; i < books.size(); i++) {
            Book book = books.get(i);
            BookReservation reservation = reservations.get(i);
            booksList.append(String.format("%d. %s (作者：%s, ISBN：%s, 預約編號：%d)\n",
                i + 1, book.getTitle(), book.getAuthor(), book.getIsbn(), reservation.getId()));
        }

        return String.format("""
            親愛的 %s 您好，

            您的批量圖書預約已成功建立！

            【預約詳細資訊】
            預約書籍數量：%d 本
            預約時間：%s
            取書期限：%s
            取書地點：%s
            取書方式：%s

            【預約書籍清單】
            %s

            【重要提醒】
            1. 請在取書期限內到館取書，逾期將自動取消預約
            2. 取書時請攜帶有效證件
            3. 如有任何問題，請聯繫圖書館服務台

            感謝您使用圖書館服務！

            此為系統自動發送，請勿回覆此郵件。
            ---
            Dear %s,

            Your batch book reservation has been successfully created!

            【Reservation Details】
            Number of Books: %d
            Reservation Time: %s
            Pickup Deadline: %s
            Pickup Location: %s
            Pickup Method: %s

            【Reserved Books List】
            %s

            【Important Reminders】
            1. Please pick up your books before the deadline
            2. Bring valid ID when picking up
            3. Contact the library service desk for any questions

            Thank you for using our library services!

            This is an automated message, please do not reply.
            """,
            member.getName(), books.size(), reservationTime, expiryTime,
            reservations.get(0).getPickupLocation(), reservations.get(0).getPickupMethod(),
            booksList.toString(),
            member.getName(), books.size(), reservationTime, expiryTime,
            reservations.get(0).getPickupLocation(), reservations.get(0).getPickupMethod(),
            booksList.toString()
        );
    }

    /**
     * 發送郵件
     */
    private void sendEmail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        message.setFrom("ispanlibrarysystem@gmail.com");
        
        mailSender.send(message);
    }
} 