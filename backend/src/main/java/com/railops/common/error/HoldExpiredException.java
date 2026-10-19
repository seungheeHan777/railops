package com.railops.common.error;

public class HoldExpiredException extends BusinessException {
    public HoldExpiredException() { super(ErrorCode.HOLD_EXPIRED); }
}
