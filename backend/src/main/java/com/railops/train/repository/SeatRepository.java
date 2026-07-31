package com.railops.train.repository;

import com.railops.train.domain.Seat;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByCar_IdOrderBySeatNoAsc(Long carId);

    boolean existsByCar_IdAndSeatNo(Long carId, String seatNo);

    boolean existsByCar_IdAndSeatNoAndIdNot(Long carId, String seatNo, Long id);
}