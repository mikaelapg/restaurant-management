package com.restaurant.management.dto.response;

import com.restaurant.management.domain.enums.UserType;
import com.restaurant.management.infrastructure.web.usertype.UserTypeResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO polimórfico simplificado de resposta para consulta unificada de Usuários.
 */
@Schema(description = "Dados consolidados de usuário")
public record UserResponse(

        @Schema(description = "ID único do usuário", example = "1")
        Long id,

        @Schema(description = "Tipo de usuário (CUSTOMER ou RESTAURANT_OWNER)", example = "CUSTOMER")
        UserType userType,

        @Schema(description = "Nome completo", example = "João da Silva")
        String name,

        @Schema(description = "E-mail de contato", example = "joao@email.com")
        String email,

        @Schema(description = "Nome de usuário para login", example = "joao.silva")
        String login,

        @Schema(description = "Data/hora da última alteração")
        LocalDateTime lastModifiedAt,

        @Schema(description = "Endereço cadastrado")
        AddressResponse address,

        @Schema(description = "Tipo de usuário associado (cadastro de tipos); nulo se não houver")
        UserTypeResponse assignedUserType
) {
}
