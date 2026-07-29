package com.railops.station.repository;

import com.railops.station.domain.Station;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StationRepository extends JpaRepository<Station, Long> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    List<Station> findByNameContainingIgnoreCaseOrCityContainingIgnoreCase(String name, String city);
}