package com.restaurant.management.core.domain.restaurant;

import com.restaurant.management.core.domain.exception.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OpeningHours")
class OpeningHoursTest {

    private static OpeningHours of(String opensAt, String closesAt) {
        return new OpeningHours(LocalTime.parse(opensAt), LocalTime.parse(closesAt));
    }

    @Test
    @DisplayName("recusa horários ausentes")
    void shouldRejectNulls() {
        assertThatThrownBy(() -> new OpeningHours(null, LocalTime.NOON))
                .isInstanceOf(DomainValidationException.class);
        assertThatThrownBy(() -> new OpeningHours(LocalTime.NOON, null))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    @DisplayName("recusa abertura igual ao fechamento")
    void shouldRejectEqualTimes() {
        assertThatThrownBy(() -> of("10:00", "10:00"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("diferentes");
    }

    @Test
    @DisplayName("identifica expediente que atravessa a meia-noite")
    void shouldDetectMidnightCrossing() {
        assertThat(of("18:00", "02:00").crossesMidnight()).isTrue();
        assertThat(of("11:00", "23:00").crossesMidnight()).isFalse();
    }

    @ParameterizedTest(name = "{0}–{1} às {2}: aberto = {3}")
    @CsvSource({
            "11:00, 23:00, 11:00, true",
            "11:00, 23:00, 15:30, true",
            "11:00, 23:00, 23:00, false",
            "11:00, 23:00, 10:59, false",
            "18:00, 02:00, 18:00, true",
            "18:00, 02:00, 23:59, true",
            "18:00, 02:00, 01:00, true",
            "18:00, 02:00, 02:00, false",
            "18:00, 02:00, 12:00, false"
    })
    @DisplayName("informa se está aberto em um horário")
    void shouldTellIfOpen(String opensAt, String closesAt, String time, boolean expected) {
        assertThat(of(opensAt, closesAt).isOpenAt(LocalTime.parse(time))).isEqualTo(expected);
    }
}
