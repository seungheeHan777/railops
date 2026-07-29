package com.railops.common.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "잘못된 요청입니다."),
    AUTH_REQUIRED(HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED", "로그인이 필요합니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "접근 권한이 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "요청한 리소스를 찾을 수 없습니다."),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "DUPLICATE_RESOURCE", "이미 존재하는 리소스입니다."),
    STATION_NOT_FOUND(HttpStatus.NOT_FOUND, "STATION_NOT_FOUND", "역 정보를 찾을 수 없습니다."),
    DUPLICATE_STATION_CODE(HttpStatus.CONFLICT, "DUPLICATE_STATION_CODE", "이미 사용 중인 역 코드입니다."),
    ROUTE_NOT_FOUND(HttpStatus.NOT_FOUND, "ROUTE_NOT_FOUND", "노선 정보를 찾을 수 없습니다."),
    DUPLICATE_ROUTE(HttpStatus.CONFLICT, "DUPLICATE_ROUTE", "이미 등록된 노선입니다."),
    INVALID_ROUTE_STATIONS(HttpStatus.BAD_REQUEST, "INVALID_ROUTE_STATIONS", "출발역과 도착역은 서로 달라야 합니다."),
    TRAIN_NOT_FOUND(HttpStatus.NOT_FOUND, "TRAIN_NOT_FOUND", "열차 정보를 찾을 수 없습니다."),
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "SCHEDULE_NOT_FOUND", "운행편 정보를 찾을 수 없습니다."),
    SCHEDULE_NOT_BOOKABLE(HttpStatus.CONFLICT, "SCHEDULE_NOT_BOOKABLE", "예매할 수 없는 운행편입니다."),
    SEAT_NOT_FOUND(HttpStatus.NOT_FOUND, "SEAT_NOT_FOUND", "좌석 정보를 찾을 수 없습니다."),
    SEAT_NOT_AVAILABLE(HttpStatus.CONFLICT, "SEAT_NOT_AVAILABLE", "선택한 좌석을 예매할 수 없습니다."),
    SEAT_ALREADY_HELD(HttpStatus.CONFLICT, "SEAT_ALREADY_HELD", "이미 임시 점유된 좌석입니다."),
    SEAT_ALREADY_RESERVED(HttpStatus.CONFLICT, "SEAT_ALREADY_RESERVED", "이미 예매된 좌석입니다."),
    SEAT_BLOCKED(HttpStatus.CONFLICT, "SEAT_BLOCKED", "판매 중지된 좌석입니다."),
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "RESERVATION_NOT_FOUND", "예매 정보를 찾을 수 없습니다."),
    RESERVATION_NOT_OWNED(HttpStatus.FORBIDDEN, "RESERVATION_NOT_OWNED", "본인 예매만 조회하거나 변경할 수 있습니다."),
    RESERVATION_NOT_PENDING_PAYMENT(HttpStatus.CONFLICT, "RESERVATION_NOT_PENDING_PAYMENT", "결제 대기 상태의 예매가 아닙니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT_NOT_FOUND", "결제 정보를 찾을 수 없습니다."),
    PAYMENT_NOT_READY(HttpStatus.CONFLICT, "PAYMENT_NOT_READY", "결제 대기 상태가 아닙니다."),
    PAYMENT_ALREADY_PROCESSED(HttpStatus.CONFLICT, "PAYMENT_ALREADY_PROCESSED", "이미 처리된 결제입니다."),
    HOLD_EXPIRED(HttpStatus.CONFLICT, "HOLD_EXPIRED", "좌석 임시 점유 시간이 만료되었습니다."),
    CONCURRENCY_CONFLICT(HttpStatus.CONFLICT, "CONCURRENCY_CONFLICT", "동시 요청으로 인해 처리에 실패했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus status() {
        return status;
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }
}