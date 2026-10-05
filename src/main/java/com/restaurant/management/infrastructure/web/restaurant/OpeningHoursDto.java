package com.restaurant.management.infrastructure.web.restaurant;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.restaurant.management.core.domain.restaurant.OpeningHours;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

/** Horário de funcionamento no formato {@code HH:mm}. */
@Schema(description = "Horário de funcionamento. O fechamento pode ser antes da abertura, quando o restaurante vira a noite.")
public record OpeningHoursDto(

        @Schema(description = "Abertura", example = "11:00", type = "string", pattern = "HH:mm")
        @JsonFormat(pattern = "HH:mm")
        @NotNull(message = "O horário de abertura é obrigatório.")
        LocalTime opensAt,

        @Schema(description = "Fechamento", example = "23:00", type = "string", pattern = "HH:mm")
        @JsonFormat(pattern = "HH:mm")
        @NotNull(message = "O horário de fechamento é obrigatório.")
        LocalTime closesAt
) {

    public OpeningHours toDomain() {
        return new OpeningHours(opensAt, closesAt);
    }

    public static OpeningHoursDto fromDomain(OpeningHours openingHours) {
        return new OpeningHoursDto(openingHours.opensAt(), openingHours.closesAt());
    }
}
