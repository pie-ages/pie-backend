package com.ages.pie.application.service;

import com.ages.pie.application.dto.UserRequestDTO;
import com.ages.pie.application.dto.UserResponseDTO;
import com.ages.pie.application.dto.UserUpdateDTO;
import com.ages.pie.application.mapper.UserMapper;
import com.ages.pie.domain.entity.User;
import com.ages.pie.infrastructure.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private UUID userId;
    private User user;
    private UserResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User("Ana Silva", "ana@email.com", "hash(senha123)");
        responseDTO = new UserResponseDTO(
                userId, "Ana Silva", "ana@email.com", null,
                OffsetDateTime.now(), OffsetDateTime.now()
        );
    }

    // -------------------------------------------------------------------------
    // create
    // -------------------------------------------------------------------------

    @Test
    void create_shouldReturnResponseDTO_whenEmailIsNew() {
        UserRequestDTO dto = new UserRequestDTO("Ana Silva", "ana@email.com", "senha123");

        when(userRepository.existsByEmail(dto.email())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponseDTO(user)).thenReturn(responseDTO);

        UserResponseDTO result = userService.create(dto);

        assertThat(result).isEqualTo(responseDTO);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void create_shouldThrowDataIntegrityViolationException_whenEmailAlreadyExists() {
        UserRequestDTO dto = new UserRequestDTO("Ana Silva", "ana@email.com", "senha123");

        when(userRepository.existsByEmail(dto.email())).thenReturn(true);

        assertThatThrownBy(() -> userService.create(dto))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("ana@email.com");

        verify(userRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // findAll
    // -------------------------------------------------------------------------

    @Test
    void findAll_shouldReturnMappedList() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toResponseDTO(user)).thenReturn(responseDTO);

        List<UserResponseDTO> result = userService.findAll();

        assertThat(result).hasSize(1).contains(responseDTO);
    }

    @Test
    void findAll_shouldReturnEmptyList_whenNoUsersExist() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserResponseDTO> result = userService.findAll();

        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------------------------
    // findById
    // -------------------------------------------------------------------------

    @Test
    void findById_shouldReturnResponseDTO_whenUserExists() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toResponseDTO(user)).thenReturn(responseDTO);

        UserResponseDTO result = userService.findById(userId);

        assertThat(result).isEqualTo(responseDTO);
    }

    @Test
    void findById_shouldThrowEntityNotFoundException_whenUserDoesNotExist() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(userId.toString());
    }

    // -------------------------------------------------------------------------
    // update
    // -------------------------------------------------------------------------

    @Test
    void update_shouldReturnUpdatedResponseDTO_whenUserExists() {
        UserUpdateDTO dto = new UserUpdateDTO("Ana Santos", "https://foto.com/ana.jpg");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponseDTO(user)).thenReturn(responseDTO);

        UserResponseDTO result = userService.update(userId, dto);

        assertThat(result).isEqualTo(responseDTO);
        verify(userRepository).save(user);
    }

    @Test
    void update_shouldThrowEntityNotFoundException_whenUserDoesNotExist() {
        UserUpdateDTO dto = new UserUpdateDTO("Ana Santos", null);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(userId, dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(userId.toString());

        verify(userRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // delete
    // -------------------------------------------------------------------------

    @Test
    void delete_shouldDeleteUser_whenUserExists() {
        when(userRepository.existsById(userId)).thenReturn(true);

        userService.delete(userId);

        verify(userRepository).deleteById(userId);
    }

    @Test
    void delete_shouldThrowEntityNotFoundException_whenUserDoesNotExist() {
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatThrownBy(() -> userService.delete(userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(userId.toString());

        verify(userRepository, never()).deleteById(any());
    }
}
