package com.restaurant.management.infrastructure.web.restaurantowner;

import com.restaurant.management.core.domain.user.RestaurantOwnerDetails;
import com.restaurant.management.core.domain.user.User;
import com.restaurant.management.core.domain.user.UserProfile;
import com.restaurant.management.infrastructure.web.shared.AddressDto;
import com.restaurant.management.infrastructure.web.usertype.UserTypeResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/** O que a API devolve sobre um dono de restaurante. A senha nunca sai na resposta. */
@Schema(description = "Dados do dono de restaurante")
public record RestaurantOwnerResponse(
        @Schema(description = "Identificador", example = "1")
        Long id,

        @Schema(description = "Perfil cadastral", example = "RESTAURANT_OWNER")
        UserProfile profile,

        @Schema(description = "Nome completo", example = "João Silva")
        String name,

        @Schema(description = "E-mail", example = "joao@restaurante.com")
        String email,

        @Schema(description = "Login", example = "joao.silva")
        String login,

        @Schema(description = "Data e hora da última alteração", example = "2026-10-22T10:00:00")
        LocalDateTime lastModifiedAt,

        @Schema(description = "Endereço; nulo se não informado")
        AddressDto address,

        @Schema(description = "Nome fantasia ou razão social da empresa do dono", example = "Grupo Silva Alimentação")
        String restaurantName,

        @Schema(description = "CNPJ", example = "12.345.678/0001-90")
        String cnpj,

        @Schema(description = "Tipo de usuário associado; nulo se não houver")
        UserTypeResponse userType
) {

    /** O controller só chama com donos; o caso de uso já filtrou o perfil. */
    public static RestaurantOwnerResponse fromDomain(User user) {
        RestaurantOwnerDetails details = (RestaurantOwnerDetails) user.getDetails();
        return new RestaurantOwnerResponse(
                user.getId(),
                user.getProfile(),
                user.getName(),
                user.getEmail(),
                user.getLogin(),
                user.getLastModifiedAt(),
                AddressDto.fromNullable(user.getAddress()),
                details.restaurantName(),
                details.cnpj(),
                UserTypeResponse.fromNullable(user.getUserType()));
    }
}
