package com.ages.pie.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import com.ages.pie.application.dto.LoginRequestDTO;
import com.ages.pie.application.dto.LoginResponseDTO;
import com.ages.pie.application.exception.NoUsersAvailableException;
import com.ages.pie.domain.entity.User;
import com.ages.pie.infrastructure.repository.UserRepository;
import com.ages.pie.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User("Ana Silva", "ana@email.com", "hash(x)");
        ReflectionTestUtils.setField(user, "id", userId);
    }

    @Test
    void loginRetornaTokenEDadosDoUsuario() {
        when(userRepository.findFirstByOrderByCreatedAtAsc()).thenReturn(Optional.of(user));
        when(jwtTokenProvider.gerarToken(userId)).thenReturn("tok-123");

        LoginResponseDTO result = authService.login(new LoginRequestDTO("ana@email.com", "senha"));

        assertThat(result.token()).isEqualTo("tok-123");
        assertThat(result.user().id()).isEqualTo(userId);
        assertThat(result.user().name()).isEqualTo("Ana Silva");
        assertThat(result.user().email()).isEqualTo("ana@email.com");
    }

    @Test
    void loginNaoValidaCredenciais() {
        when(userRepository.findFirstByOrderByCreatedAtAsc()).thenReturn(Optional.of(user));
        when(jwtTokenProvider.gerarToken(any())).thenReturn("tok");

        LoginResponseDTO result = authService.login(new LoginRequestDTO(null, null));

        assertThat(result.token()).isEqualTo("tok");
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void loginUsaOPrimeiroUsuarioDisponivel() {
        when(userRepository.findFirstByOrderByCreatedAtAsc()).thenReturn(Optional.of(user));
        when(jwtTokenProvider.gerarToken(any())).thenReturn("tok");

        authService.login(new LoginRequestDTO("x", "y"));

        verify(userRepository).findFirstByOrderByCreatedAtAsc();
        verify(userRepository, never()).findAll();
    }

    @Test
    void loginGeraTokenComOIdDoUsuarioEncontrado() {
        when(userRepository.findFirstByOrderByCreatedAtAsc()).thenReturn(Optional.of(user));
        when(jwtTokenProvider.gerarToken(any())).thenReturn("tok");

        authService.login(new LoginRequestDTO("x", "y"));

        ArgumentCaptor<UUID> captor = ArgumentCaptor.forClass(UUID.class);
        verify(jwtTokenProvider).gerarToken(captor.capture());
        assertThat(captor.getValue()).isEqualTo(userId);
    }

    @Test
    void loginLancaQuandoNaoHaUsuarios() {
        when(userRepository.findFirstByOrderByCreatedAtAsc()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequestDTO("x", "y")))
            .isInstanceOf(NoUsersAvailableException.class)
            .hasMessage("Nenhum usuário cadastrado");

        verify(jwtTokenProvider, never()).gerarToken(any());
    }
}
