package com.railops;

import static org.assertj.core.api.Assertions.assertThat;

import com.railops.common.response.ApiResponse;
import org.junit.jupiter.api.Test;

class RailOpsApplicationTests {

    @Test
    void apiResponseCreatesSuccessBody() {
        ApiResponse<String> response = ApiResponse.ok("railops");

        assertThat(response.success()).isTrue();
        assertThat(response.data()).isEqualTo("railops");
        assertThat(response.message()).isNull();
        assertThat(response.code()).isNull();
    }

    @Test
    void apiResponseCreatesErrorBody() {
        ApiResponse<Void> response = ApiResponse.error("잘못된 요청입니다.", "INVALID_REQUEST");

        assertThat(response.success()).isFalse();
        assertThat(response.data()).isNull();
        assertThat(response.message()).isEqualTo("잘못된 요청입니다.");
        assertThat(response.code()).isEqualTo("INVALID_REQUEST");
    }
}