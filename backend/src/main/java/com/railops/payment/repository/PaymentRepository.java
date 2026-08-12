package com.railops.payment.repository;

import com.railops.payment.domain.Payment;
import com.railops.payment.domain.PaymentStatus;
import com.railops.reservation.domain.ReservationStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select payment
        from Payment payment
        join fetch payment.reservation reservation
        join fetch reservation.user user
        where payment.id = :paymentId
        """)
    Optional<Payment> findByIdForUpdate(@Param("paymentId") Long paymentId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Payment payment
        set payment.status = :expiredPaymentStatus,
            payment.expiredAt = :now,
            payment.updatedAt = :now
        where payment.status = :readyPaymentStatus
          and payment.reservation.status = :pendingReservationStatus
          and payment.reservation.holdExpiresAt < :now
        """)
    int expireReadyPayments(
        @Param("readyPaymentStatus") PaymentStatus readyPaymentStatus,
        @Param("expiredPaymentStatus") PaymentStatus expiredPaymentStatus,
        @Param("pendingReservationStatus") ReservationStatus pendingReservationStatus,
        @Param("now") LocalDateTime now
    );
}