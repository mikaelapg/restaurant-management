package com.restaurant.management.infrastructure.web.usertype;

import com.restaurant.management.core.application.exception.UserNotFoundException;
import com.restaurant.management.core.application.exception.UserTypeNotFoundException;
import com.restaurant.management.core.application.usertype.AssignUserTypeUseCase;
import com.restaurant.management.domain.enums.UserType;
import com.restaurant.management.dto.response.UserResponse;
import com.restaurant.management.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserTypeAssignmentController.class)
@DisplayName("UserTypeAssignmentController")
class UserTypeAssignmentControllerTest {

    private static final String URL = "/api/v1/users/10/user-type";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AssignUserTypeUseCase assignUserType;
    @MockBean
    private UserService userService;

    @Test
    @DisplayName("PUT associa e devolve o usuário com o tipo novo")
    void shouldAssign() throws Exception {
        when(userService.findById(10L)).thenReturn(new UserResponse(10L, UserType.CUSTOMER, "Maria",
                "maria@email.com", "maria", LocalDateTime.of(2026, 10, 1, 12, 0), null,
                new UserTypeResponse(3L, "Cliente VIP")));

        mockMvc.perform(put(URL).contentType(MediaType.APPLICATION_JSON).content("{\"userTypeId\": 3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.assignedUserType.id").value(3))
                .andExpect(jsonPath("$.assignedUserType.name").value("Cliente VIP"));

        verify(assignUserType).execute(10L, 3L);
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
    @DisplayName("PUT com usuário inexistente responde 404 user-not-found")
    void shouldReturnNotFoundForUser() throws Exception {
        doThrow(new UserNotFoundException(10L)).when(assignUserType).execute(10L, 3L);

        mockMvc.perform(put(URL).contentType(MediaType.APPLICATION_JSON).content("{\"userTypeId\": 3}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("https://restaurant-api.com/errors/user-not-found"));
    }

    @Test
    @DisplayName("PUT com tipo inexistente responde 404 user-type-not-found")
    void shouldReturnNotFoundForUserType() throws Exception {
        doThrow(new UserTypeNotFoundException(3L)).when(assignUserType).execute(10L, 3L);

        mockMvc.perform(put(URL).contentType(MediaType.APPLICATION_JSON).content("{\"userTypeId\": 3}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("https://restaurant-api.com/errors/user-type-not-found"));
    }
}
