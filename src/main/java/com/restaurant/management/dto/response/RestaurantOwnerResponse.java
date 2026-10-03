package com.restaurant.management.dto.response;

import com.restaurant.management.domain.enums.UserType;
import com.restaurant.management.infrastructure.web.usertype.UserTypeResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO de resposta para Dono de Restaurante.
 * A senha nunca é incluída em respostas da API.
 */
@Schema(description = "Dados do dono de restaurante")
public record RestaurantOwnerResponse(

        @Schema(description = "ID único", example = "1")
        Long id,

        @Schema(description = "Tipo de usuário", example = "RESTAURANT_OWNER")
        UserType userType,

        @Schema(description = "Nome completo", example = "João Silva")
        String name,

        @Schema(description = "E-mail", example = "joao@restaurante.com")
        String email,

        @Schema(description = "Login", example = "joao.silva")
        String login,

        @Schema(description = "Data/hora da última alteração", example = "2024-08-12T21:00:00")
        LocalDateTime lastModifiedAt,

        @Schema(description = "Endereço")
        AddressResponse address,

        @Schema(description = "Nome do restaurante", example = "Restaurante do João")
        String restaurantName,

        @Schema(description = "CNPJ do restaurante", example = "12.345.678/0001-90")
        String cnpj,

        @Schema(description = "Tipo de usuário associado (cadastro de tipos); nulo se não houver")
        UserTypeResponse assignedUserType
) {
}
