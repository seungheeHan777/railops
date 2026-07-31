package com.railops.train.service;

import com.railops.common.error.BusinessException;
import com.railops.common.error.ErrorCode;
import com.railops.train.domain.Train;
import com.railops.train.dto.TrainCreateRequest;
import com.railops.train.dto.TrainResponse;
import com.railops.train.dto.TrainUpdateRequest;
import com.railops.train.repository.TrainRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TrainService {

    private final TrainRepository trainRepository;

    public TrainService(TrainRepository trainRepository) {
        this.trainRepository = trainRepository;
    }

    public List<TrainResponse> findAll() {
        return trainRepository.findAll()
            .stream()
            .map(TrainResponse::from)
            .toList();
    }

    public TrainResponse get(Long trainId) {
        return TrainResponse.from(getTrain(trainId));
    }

    @Transactional
    public TrainResponse create(TrainCreateRequest request) {
        String trainNo = normalizeTrainNo(request.trainNo());
        if (trainRepository.existsByTrainNo(trainNo)) {
            throw new BusinessException(ErrorCode.DUPLICATE_TRAIN_NO);
        }

        Train train = Train.create(trainNo, request.trainType(), request.name());
        return TrainResponse.from(trainRepository.save(train));
    }

    @Transactional
    public TrainResponse update(Long trainId, TrainUpdateRequest request) {
        Train train = getTrain(trainId);
        String trainNo = normalizeTrainNo(request.trainNo());

        if (trainRepository.existsByTrainNoAndIdNot(trainNo, trainId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_TRAIN_NO);
        }

        train.update(trainNo, request.trainType(), request.name());
        return TrainResponse.from(train);
    }

    @Transactional
    public void delete(Long trainId) {
        Train train = getTrain(trainId);
        trainRepository.delete(train);
    }

    private Train getTrain(Long trainId) {
        return trainRepository.findById(trainId)
            .orElseThrow(() -> new BusinessException(ErrorCode.TRAIN_NOT_FOUND));
    }

    private String normalizeTrainNo(String trainNo) {
        return trainNo == null ? null : trainNo.trim().toUpperCase();
    }
}