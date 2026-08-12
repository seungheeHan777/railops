package com.railops.reservation.domain;

public enum ReservationStatus {
    PENDING_PAYMENT,
    CONFIRMED,
    CANCELED,
    EXPIRED,
    PAYMENT_FAILED
}