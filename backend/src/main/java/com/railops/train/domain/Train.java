package com.railops.train.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "trains")
public class Train {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String trainNo;

    @Column(nullable = false)
    private String trainType;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected Train() {
    }

    private Train(String trainNo, String trainType, String name) {
        LocalDateTime now = LocalDateTime.now();
        this.trainNo = normalizeTrainNo(trainNo);
        this.trainType = normalizeTrainType(trainType);
        this.name = name;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static Train create(String trainNo, String trainType, String name) {
        return new Train(trainNo, trainType, name);
    }

    public void update(String trainNo, String trainType, String name) {
        this.trainNo = normalizeTrainNo(trainNo);
        this.trainType = normalizeTrainType(trainType);
        this.name = name;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getTrainNo() {
        return trainNo;
    }

    public String getTrainType() {
        return trainType;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    private static String normalizeTrainNo(String trainNo) {
        return trainNo == null ? null : trainNo.trim().toUpperCase();
    }

    private static String normalizeTrainType(String trainType) {
        return trainType == null ? null : trainType.trim().toUpperCase();
    }
}