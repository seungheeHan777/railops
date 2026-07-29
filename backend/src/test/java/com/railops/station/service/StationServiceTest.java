package com.railops.station.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.railops.common.error.BusinessException;
import com.railops.common.error.ErrorCode;
import com.railops.station.domain.Station;
import com.railops.station.dto.StationCreateRequest;
import com.railops.station.dto.StationResponse;
import com.railops.station.dto.StationUpdateRequest;
import com.railops.station.repository.StationRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StationServiceTest {

    @Mock
    private StationRepository stationRepository;

    private StationService stationService;

    @BeforeEach
    void setUp() {
        stationService = new StationService(stationRepository);
    }

    @Test
    void createStation() {
        StationCreateRequest request = new StationCreateRequest("서울", "seoul", "서울");
        Station station = Station.create(request.name(), request.code(), request.city());

        when(stationRepository.existsByCode("SEOUL")).thenReturn(false);
        when(stationRepository.save(any(Station.class))).thenReturn(station);

        StationResponse response = stationService.create(request);

        assertThat(response.name()).isEqualTo("서울");
        assertThat(response.code()).isEqualTo("SEOUL");
        assertThat(response.city()).isEqualTo("서울");
    }

    @Test
    void createStationRejectsDuplicateCode() {
        StationCreateRequest request = new StationCreateRequest("서울", "SEOUL", "서울");
        when(stationRepository.existsByCode("SEOUL")).thenReturn(true);

        assertThatThrownBy(() -> stationService.create(request))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.DUPLICATE_STATION_CODE);
    }

    @Test
    void searchReturnsAllWhenKeywordIsBlank() {
        when(stationRepository.findAll()).thenReturn(List.of(Station.create("서울", "SEOUL", "서울")));

        List<StationResponse> responses = stationService.search(" ");

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().code()).isEqualTo("SEOUL");
    }

    @Test
    void getRejectsMissingStation() {
        when(stationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stationService.get(1L))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.STATION_NOT_FOUND);
    }

    @Test
    void updateStation() {
        Station station = Station.create("서울", "SEOUL", "서울");
        StationUpdateRequest request = new StationUpdateRequest("수서", "suseo", "서울");

        when(stationRepository.findById(1L)).thenReturn(Optional.of(station));
        when(stationRepository.existsByCodeAndIdNot("SUSEO", 1L)).thenReturn(false);

        StationResponse response = stationService.update(1L, request);

        assertThat(response.name()).isEqualTo("수서");
        assertThat(response.code()).isEqualTo("SUSEO");
    }

    @Test
    void deleteStation() {
        Station station = Station.create("서울", "SEOUL", "서울");
        when(stationRepository.findById(1L)).thenReturn(Optional.of(station));

        stationService.delete(1L);

        verify(stationRepository).delete(station);
    }
}