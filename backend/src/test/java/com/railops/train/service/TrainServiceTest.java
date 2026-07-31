package com.railops.train.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.railops.common.error.BusinessException;
import com.railops.common.error.ErrorCode;
import com.railops.train.domain.Train;
import com.railops.train.dto.TrainCreateRequest;
import com.railops.train.dto.TrainResponse;
import com.railops.train.dto.TrainUpdateRequest;
import com.railops.train.repository.TrainRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrainServiceTest {

    @Mock
    private TrainRepository trainRepository;

    private TrainService trainService;

    @BeforeEach
    void setUp() {
        trainService = new TrainService(trainRepository);
    }

    @Test
    void createTrain() {
        TrainCreateRequest request = new TrainCreateRequest("ktx-101", "ktx", "KTX 101");
        Train train = Train.create(request.trainNo(), request.trainType(), request.name());

        when(trainRepository.existsByTrainNo("KTX-101")).thenReturn(false);
        when(trainRepository.save(any(Train.class))).thenReturn(train);

        TrainResponse response = trainService.create(request);

        assertThat(response.trainNo()).isEqualTo("KTX-101");
        assertThat(response.trainType()).isEqualTo("KTX");
        assertThat(response.name()).isEqualTo("KTX 101");
    }

    @Test
    void createTrainRejectsDuplicateTrainNo() {
        TrainCreateRequest request = new TrainCreateRequest("KTX-101", "KTX", "KTX 101");
        when(trainRepository.existsByTrainNo("KTX-101")).thenReturn(true);

        assertThatThrownBy(() -> trainService.create(request))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.DUPLICATE_TRAIN_NO);
    }

    @Test
    void findAllTrains() {
        when(trainRepository.findAll()).thenReturn(List.of(Train.create("KTX-101", "KTX", "KTX 101")));

        List<TrainResponse> responses = trainService.findAll();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).trainNo()).isEqualTo("KTX-101");
    }

    @Test
    void getRejectsMissingTrain() {
        when(trainRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainService.get(1L))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.TRAIN_NOT_FOUND);
    }

    @Test
    void updateTrain() {
        Train train = Train.create("KTX-101", "KTX", "KTX 101");
        TrainUpdateRequest request = new TrainUpdateRequest("itx-201", "itx", "ITX 201");

        when(trainRepository.findById(1L)).thenReturn(Optional.of(train));
        when(trainRepository.existsByTrainNoAndIdNot("ITX-201", 1L)).thenReturn(false);

        TrainResponse response = trainService.update(1L, request);

        assertThat(response.trainNo()).isEqualTo("ITX-201");
        assertThat(response.trainType()).isEqualTo("ITX");
        assertThat(response.name()).isEqualTo("ITX 201");
    }

    @Test
    void updateTrainRejectsDuplicateTrainNo() {
        Train train = Train.create("KTX-101", "KTX", "KTX 101");
        TrainUpdateRequest request = new TrainUpdateRequest("KTX-102", "KTX", "KTX 102");

        when(trainRepository.findById(1L)).thenReturn(Optional.of(train));
        when(trainRepository.existsByTrainNoAndIdNot("KTX-102", 1L)).thenReturn(true);

        assertThatThrownBy(() -> trainService.update(1L, request))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.DUPLICATE_TRAIN_NO);
    }

    @Test
    void deleteTrain() {
        Train train = Train.create("KTX-101", "KTX", "KTX 101");
        when(trainRepository.findById(1L)).thenReturn(Optional.of(train));

        trainService.delete(1L);

        verify(trainRepository).delete(train);
    }
}