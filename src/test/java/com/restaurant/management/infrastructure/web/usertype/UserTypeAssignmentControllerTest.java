package com.restaurant.management.infrastructure.web.usertype;

import com.restaurant.management.core.application.exception.UserNotFoundException;
import com.restaurant.management.core.application.exception.UserTypeChangeNotAllowedException;
import com.restaurant.management.core.application.exception.UserTypeNotFoundException;
import com.restaurant.management.core.application.usertype.AssignUserTypeUseCase;
import com.restaurant.management.core.domain.exception.IncompatibleUserTypeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static com.restaurant.management.support.Fixtures.CUSTOM_TYPE;
import static com.restaurant.management.support.Fixtures.customer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserTypeAssignmentController.class)
@DisplayName("UserTypeAssignmentController")
class UserTypeAssignmentControllerTest {

    private static final String URL = "/api/v1/users/10/user-type";
    private static final String BODY = "{\"userTypeId\": 3}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AssignUserTypeUseCase assignUserType;

    @Test
    @DisplayName("PUT associa e devolve o usuário com o tipo novo")
    void shouldAssign() throws Exception {
        when(assignUserType.execute(10L, 3L)).thenReturn(customer(10L, CUSTOM_TYPE));

        mockMvc.perform(put(URL).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.profile").value("CUSTOMER"))
                .andExpect(jsonPath("$.userType.id").value(3))
                .andExpect(jsonPath("$.userType.name").value("Gerente"))
                .andExpect(jsonPath("$.userType.systemType").value(false));
    }

    @Test
    @DisplayName("PUT sem userTypeId responde 400 sem chamar o caso de uso")
    void shouldRejectMissingUserTypeId() throws Exception {
        mockMvc.perform(put(URL).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.userTypeId").exists());

        verifyNoInteractions(assignUserType);
    }

    @Test
    @DisplayName("PUT com usuário ou tipo inexistente responde 404 com o type de cada um")
    void shouldReturnNotFound() throws Exception {
        when(assignUserType.execute(10L, 3L)).thenThrow(new UserNotFoundException(10L));
        mockMvc.perform(put(URL).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("https://restaurant-api.com/errors/user-not-found"));

        doThrow(new UserTypeNotFoundException(3L)).when(assignUserType).execute(10L, 3L);
        mockMvc.perform(put(URL).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("https://restaurant-api.com/errors/user-type-not-found"));
    }

    @Test
    @DisplayName("PUT com tipo de sistema de outro perfil responde 422")
    void shouldReturnUnprocessableForIncompatibleType() throws Exception {
        when(assignUserType.execute(10L, 3L)).thenThrow(new IncompatibleUserTypeException("incompatível"));

        mockMvc.perform(put(URL).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.type").value("https://restaurant-api.com/errors/incompatible-user-type"));
    }

    @Test
    @DisplayName("PUT que tiraria o tipo de dono de quem tem restaurantes responde 409")
    void shouldReturnConflictWhenOwnerHasRestaurants() throws Exception {
        when(assignUserType.execute(10L, 3L)).thenThrow(new UserTypeChangeNotAllowedException(10L));

        mockMvc.perform(put(URL).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("https://restaurant-api.com/errors/user-type-change-not-allowed"));
    }
}
