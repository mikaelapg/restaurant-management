package com.restaurant.management.core.domain.restaurant;

import com.restaurant.management.core.domain.exception.DomainValidationException;

import java.time.LocalTime;

/**
 * Horário de funcionamento: hora de abertura e de fechamento.
 * <p>
 * O fechamento pode ser anterior à abertura, o que significa que o restaurante vira a
 * noite (das 18:00 às 02:00, por exemplo). Abertura e fechamento iguais são recusados,
 * porque não dá para saber se o restaurante nunca fecha ou nunca abre.
 */
public record OpeningHours(LocalTime opensAt, LocalTime closesAt) {

    public OpeningHours {
        if (opensAt == null || closesAt == null) {
            throw new DomainValidationException("Os horários de abertura e de fechamento são obrigatórios.");
        }
        if (opensAt.equals(closesAt)) {
            throw new DomainValidationException("Os horários de abertura e de fechamento devem ser diferentes.");
        }
    }

    /** Indica se o expediente atravessa a meia-noite. */
    public boolean crossesMidnight() {
        return closesAt.isBefore(opensAt);
    }

    /** Indica se o restaurante está aberto no horário informado. A abertura conta; o fechamento, não. */
    public boolean isOpenAt(LocalTime time) {
        if (crossesMidnight()) {
            return !time.isBefore(opensAt) || time.isBefore(closesAt);
        }
        return !time.isBefore(opensAt) && time.isBefore(closesAt);
    }
}
