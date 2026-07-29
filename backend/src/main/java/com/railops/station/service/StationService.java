package com.railops.station.service;

import com.railops.common.error.BusinessException;
import com.railops.common.error.ErrorCode;
import com.railops.station.domain.Station;
import com.railops.station.dto.StationCreateRequest;
import com.railops.station.dto.StationResponse;
import com.railops.station.dto.StationUpdateRequest;
import com.railops.station.repository.StationRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StationService {

    private final StationRepository stationRepository;

    public StationService(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    public List<StationResponse> findAll() {
        return stationRepository.findAll()
            .stream()
            .map(StationResponse::from)
            .toList();
    }

    public List<StationResponse> search(String keyword) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        if (normalizedKeyword.isEmpty()) {
            return findAll();
        }

        return stationRepository.findByNameContainingIgnoreCaseOrCityContainingIgnoreCase(
                normalizedKeyword,
                normalizedKeyword
            )
            .stream()
            .map(StationResponse::from)
            .toList();
    }

    public StationResponse get(Long stationId) {
        return StationResponse.from(getStation(stationId));
    }

    @Transactional
    public StationResponse create(StationCreateRequest request) {
        String code = normalizeCode(request.code());
        if (stationRepository.existsByCode(code)) {
            throw new BusinessException(ErrorCode.DUPLICATE_STATION_CODE);
        }

        Station station = Station.create(request.name(), code, request.city());
        return StationResponse.from(stationRepository.save(station));
    }

    @Transactional
    public StationResponse update(Long stationId, StationUpdateRequest request) {
        Station station = getStation(stationId);
        String code = normalizeCode(request.code());

        if (stationRepository.existsByCodeAndIdNot(code, stationId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_STATION_CODE);
        }

        station.update(request.name(), code, request.city());
        return StationResponse.from(station);
    }

    @Transactional
    public void delete(Long stationId) {
        Station station = getStation(stationId);
        stationRepository.delete(station);
    }

    private Station getStation(Long stationId) {
        return stationRepository.findById(stationId)
            .orElseThrow(() -> new BusinessException(ErrorCode.STATION_NOT_FOUND));
    }

    private String normalizeCode(String code) {
        return code == null ? null : code.trim().toUpperCase();
    }
}