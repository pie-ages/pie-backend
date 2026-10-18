package com.ages.pie.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import com.ages.pie.application.dto.LoginRequestDTO;
import com.ages.pie.application.dto.LoginResponseDTO;
import com.ages.pie.application.exception.InvalidCredentialsException;
import com.ages.pie.domain.entity.User;
import com.ages.pie.infrastructure.auth.SupabaseAuthClient;
import com.ages.pie.infrastructure.auth.SupabaseAuthClient.Session;
import com.ages.pie.infrastructure.auth.SupabaseAuthClient.SessionUser;
import com.ages.pie.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private SupabaseAuthClient supabaseAuthClient;

    @Mock
    private UserRepository userRepository;

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

    private Session session(String access, String refresh) {
        return new Session(access, refresh, 3600, "bearer", new SessionUser(userId, "ana@email.com"));
    }

    @Test
    void loginRetornaTokensEDadosDoCustomerDoSub() {
        when(supabaseAuthClient.passwordGrant("ana@email.com", "senha")).thenReturn(session("acc", "ref"));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        LoginResponseDTO result = authService.login(new LoginRequestDTO("ana@email.com", "senha"));

        assertThat(result.accessToken()).isEqualTo("acc");
        assertThat(result.refreshToken()).isEqualTo("ref");
        assertThat(result.expiresIn()).isEqualTo(3600);
        assertThat(result.tokenType()).isEqualTo("bearer");
        assertThat(result.user().id()).isEqualTo(userId);
        assertThat(result.user().name()).isEqualTo("Ana Silva");
    }

    @Test
    void loginRetornaUserNuloQuandoCustomerAindaNaoFoiCadastrado() {
        when(supabaseAuthClient.passwordGrant("ana@email.com", "senha")).thenReturn(session("acc", "ref"));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        LoginResponseDTO result = authService.login(new LoginRequestDTO("ana@email.com", "senha"));

        assertThat(result.accessToken()).isEqualTo("acc");
        assertThat(result.user()).isNull();
    }

    @Test
    void loginPropagaCredenciaisInvalidas() {
        when(supabaseAuthClient.passwordGrant("ana@email.com", "errada"))
            .thenThrow(new InvalidCredentialsException("Credenciais inválidas"));

        assertThatThrownBy(() -> authService.login(new LoginRequestDTO("ana@email.com", "errada")))
            .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void refreshRetornaNovoParDeTokens() {
        when(supabaseAuthClient.refresh("ref-antigo")).thenReturn(session("acc-novo", "ref-novo"));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        LoginResponseDTO result = authService.refresh("ref-antigo");

        assertThat(result.accessToken()).isEqualTo("acc-novo");
        assertThat(result.refreshToken()).isEqualTo("ref-novo");
    }

    @Test
    void logoutDelegaAoSupabaseComOAccessToken() {
        authService.logout("acc");

        verify(supabaseAuthClient).logout("acc");
    }
}
