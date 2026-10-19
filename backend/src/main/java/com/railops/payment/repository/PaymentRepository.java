package com.railops.payment.repository;

import com.railops.payment.domain.Payment;
import com.railops.payment.domain.PaymentStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payment p where p.id = :paymentId")
    Optional<Payment> findByIdForUpdate(@Param("paymentId") Long paymentId);

    Optional<Payment> findByReservation_Id(Long reservationId);

    @Query("select p.id from Payment p where p.reservation.id = :reservationId")
    Optional<Long> findIdByReservationId(@Param("reservationId") Long reservationId);

    @Query("select p.id from Payment p where p.status = :status and p.reservation.holdExpiresAt <= :now order by p.id")
    List<Long> findExpiredCandidateIds(@Param("status") PaymentStatus status,
        @Param("now") LocalDateTime now, Pageable pageable);
}
