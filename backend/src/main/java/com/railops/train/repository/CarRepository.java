package com.railops.train.repository;

import com.railops.train.domain.Car;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarRepository extends JpaRepository<Car, Long> {

    List<Car> findByTrain_IdOrderByCarNoAsc(Long trainId);

    boolean existsByTrain_IdAndCarNo(Long trainId, Integer carNo);

    boolean existsByTrain_IdAndCarNoAndIdNot(Long trainId, Integer carNo, Long id);
}