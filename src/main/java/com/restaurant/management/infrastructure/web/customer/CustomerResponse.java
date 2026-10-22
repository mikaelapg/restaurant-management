package com.restaurant.management.infrastructure.web.customer;

import com.restaurant.management.core.domain.user.CustomerDetails;
import com.restaurant.management.core.domain.user.User;
import com.restaurant.management.core.domain.user.UserProfile;
import com.restaurant.management.infrastructure.web.shared.AddressDto;
import com.restaurant.management.infrastructure.web.usertype.UserTypeResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** O que a API devolve sobre um cliente. A senha nunca sai na resposta. */
@Schema(description = "Dados do cliente")
public record CustomerResponse(
        @Schema(description = "Identificador", example = "2")
        Long id,

        @Schema(description = "Perfil cadastral", example = "CUSTOMER")
        UserProfile profile,

        @Schema(description = "Nome completo", example = "Maria Oliveira")
        String name,

        @Schema(description = "E-mail", example = "maria@email.com")
        String email,

        @Schema(description = "Login", example = "maria.oliveira")
        String login,

        @Schema(description = "Data e hora da última alteração", example = "2026-10-22T10:00:00")
        LocalDateTime lastModifiedAt,

        @Schema(description = "Endereço; nulo se não informado")
        AddressDto address,

        @Schema(description = "CPF", example = "123.456.789-00")
        String cpf,

        @Schema(description = "Data de nascimento", example = "1995-07-15", type = "string")
        LocalDate dateOfBirth,

        @Schema(description = "Tipo de usuário associado; nulo se não houver")
        UserTypeResponse userType
) {

    /** O controller só chama com clientes; o caso de uso já filtrou o perfil. */
    public static CustomerResponse fromDomain(User user) {
        CustomerDetails details = (CustomerDetails) user.getDetails();
        return new CustomerResponse(
                user.getId(),
                user.getProfile(),
                user.getName(),
                user.getEmail(),
                user.getLogin(),
                user.getLastModifiedAt(),
                AddressDto.fromNullable(user.getAddress()),
                details.cpf(),
                details.dateOfBirth(),
                UserTypeResponse.fromNullable(user.getUserType()));
    }
}
