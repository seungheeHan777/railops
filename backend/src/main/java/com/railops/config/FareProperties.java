package com.railops.config;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "railops.fare")
public record FareProperties(
    @DecimalMin("1") @Digits(integer = 9, fraction = 0) BigDecimal defaultSeatPrice
) {
    public FareProperties {
        if (defaultSeatPrice == null) defaultSeatPrice = new BigDecimal("50000");
    }
}
