package com.railops.train.repository;

import com.railops.train.domain.Train;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainRepository extends JpaRepository<Train, Long> {

    boolean existsByTrainNo(String trainNo);

    boolean existsByTrainNoAndIdNot(String trainNo, Long id);
}