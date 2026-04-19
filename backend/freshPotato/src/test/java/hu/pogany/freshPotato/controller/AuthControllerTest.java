package hu.pogany.freshPotato.controller;

import hu.pogany.freshPotato.dto.response.UserDtoPublic;
import hu.pogany.freshPotato.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Test
    void getUserPublicById_shouldReturnPublicProfileFromService() {
        UserDtoPublic expected = new UserDtoPublic(
                12,
                "neo",
                "male",
                26,
                List.of(),
                List.of(),
                List.of()
        );
        when(userService.getByUserIdPublic(12)).thenReturn(expected);

        AuthController authController = new AuthController(null, userService);

        UserDtoPublic result = authController.getUserPublicById(12);

        assertEquals(expected, result);
        verify(userService).getByUserIdPublic(12);
    }
}

