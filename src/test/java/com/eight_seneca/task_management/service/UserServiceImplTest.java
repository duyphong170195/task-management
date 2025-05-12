package com.eight_seneca.task_management.service;

import com.eight_seneca.common.exception.CustomException;
import com.eight_seneca.task_management.repository.UserRepository;
import com.eight_seneca.task_management.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private UUID userId;
    private String messageCode;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        messageCode = "USER_NOT_FOUND";
    }

    @Test
    void test__verifyUserExist__UserExists__DoesNotThrowException() {
        // Arrange
        when(userRepository.existsById(userId)).thenReturn(true);

        // Act & Assert
        assertDoesNotThrow(() -> userService.verifyUserExist(userId, messageCode));
    }

    @Test
    void test__verifyUserExist__UserDoesNotExist__ThrowsCustomException() {
        // Arrange
        when(userRepository.existsById(userId)).thenReturn(false);

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class,
            () -> userService.verifyUserExist(userId, messageCode));
        
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals(messageCode, exception.getCode());
    }
}