package com.railops.train.repository;

import com.railops.train.domain.Seat;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByCar_IdOrderBySeatNoAsc(Long carId);

    @Query("""
        select seat
        from Seat seat
        join fetch seat.car car
        where car.train.id = :trainId
        order by car.carNo asc, seat.seatNo asc
        """)
    List<Seat> findByTrainIdForScheduleSeatCreation(@Param("trainId") Long trainId);

    boolean existsByCar_IdAndSeatNo(Long carId, String seatNo);

    boolean existsByCar_IdAndSeatNoAndIdNot(Long carId, String seatNo, Long id);
}