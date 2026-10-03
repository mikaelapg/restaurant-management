package com.restaurant.management.dto.response;

import com.restaurant.management.domain.enums.UserType;
import com.restaurant.management.infrastructure.web.usertype.UserTypeResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de resposta para Cliente.
 * A senha nunca é incluída em respostas da API.
 */
@Schema(description = "Dados do cliente")
public record CustomerResponse(

        @Schema(description = "ID único", example = "2")
        Long id,

        @Schema(description = "Tipo de usuário", example = "CUSTOMER")
        UserType userType,

        @Schema(description = "Nome completo", example = "Maria Oliveira")
        String name,

        @Schema(description = "E-mail", example = "maria@email.com")
        String email,

        @Schema(description = "Login", example = "maria.oliveira")
        String login,

        @Schema(description = "Data/hora da última alteração", example = "2024-08-12T21:00:00")
        LocalDateTime lastModifiedAt,

        @Schema(description = "Endereço")
        AddressResponse address,

        @Schema(description = "CPF do cliente", example = "123.456.789-00")
        String cpf,

        @Schema(description = "Data de nascimento", example = "1995-07-15", type = "string")
        LocalDate dateOfBirth,

        @Schema(description = "Tipo de usuário associado (cadastro de tipos); nulo se não houver")
        UserTypeResponse assignedUserType
) {
}
