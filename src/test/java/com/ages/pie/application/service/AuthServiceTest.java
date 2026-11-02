package com.ages.pie.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import com.ages.pie.application.dto.LoginRequestDTO;
import com.ages.pie.application.dto.LoginResponseDTO;
import com.ages.pie.application.exception.InvalidCredentialsException;
import com.ages.pie.domain.entity.Company;
import com.ages.pie.domain.entity.User;
import com.ages.pie.infrastructure.auth.SupabaseAuthClient;
import com.ages.pie.infrastructure.auth.SupabaseAuthClient.Session;
import com.ages.pie.infrastructure.auth.SupabaseAuthClient.SessionUser;
import com.ages.pie.infrastructure.repository.CompanyRepository;
import com.ages.pie.infrastructure.repository.UserRepository;
import com.ages.pie.infrastructure.security.AuthenticatedUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private SupabaseAuthClient supabaseAuthClient;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @InjectMocks
    private AuthService authService;

    private UUID userId;
    private UUID supabaseUserId;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        supabaseUserId = UUID.randomUUID();
        user = new User("Ana Silva", "ana@email.com", supabaseUserId);
        ReflectionTestUtils.setField(user, "id", userId);
    }

    private Session session(String access, String refresh) {
        return new Session(access, refresh, 3600, "bearer", new SessionUser(supabaseUserId, "ana@email.com"));
    }

    @Test
    void loginResolveOCustomerLocalPeloSub() {
        when(supabaseAuthClient.passwordGrant("ana@email.com", "senha")).thenReturn(session("acc", "ref"));
        when(userRepository.findBySupabaseUserId(supabaseUserId)).thenReturn(Optional.of(user));

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
        when(userRepository.findBySupabaseUserId(supabaseUserId)).thenReturn(Optional.empty());

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
        when(userRepository.findBySupabaseUserId(supabaseUserId)).thenReturn(Optional.of(user));

        LoginResponseDTO result = authService.refresh("ref-antigo");

        assertThat(result.accessToken()).isEqualTo("acc-novo");
        assertThat(result.refreshToken()).isEqualTo("ref-novo");
    }

    @Test
    void logoutDelegaAoSupabaseComOAccessToken() {
        authService.logout("acc");

        verify(supabaseAuthClient).logout("acc");
    }

    @Test
    void recoverDelegaAoSupabaseComOEmail() {
        authService.recover("ana@email.com");

        verify(supabaseAuthClient).recover("ana@email.com");
    }

    @Test
    void verifyRecoveryResolveOCustomerPeloSub() {
        when(supabaseAuthClient.verifyRecovery("ana@email.com", "123456")).thenReturn(session("acc", "ref"));
        when(userRepository.findBySupabaseUserId(supabaseUserId)).thenReturn(Optional.of(user));

        LoginResponseDTO result = authService.verifyRecovery("ana@email.com", "123456");

        assertThat(result.accessToken()).isEqualTo("acc");
        assertThat(result.refreshToken()).isEqualTo("ref");
        assertThat(result.user().id()).isEqualTo(userId);
    }

    @Test
    void verifyRecoveryResolveAEmpresaPeloSub() {
        UUID companyId = UUID.randomUUID();
        Company company = new Company(companyId, "Loja X", "12345678000199", "Loja X LTDA",
            "Maria", "ana@email.com", supabaseUserId, null, null);
        when(supabaseAuthClient.verifyRecovery("ana@email.com", "123456")).thenReturn(session("acc", "ref"));
        when(userRepository.findBySupabaseUserId(supabaseUserId)).thenReturn(Optional.empty());
        when(companyRepository.findBySupabaseUserId(supabaseUserId)).thenReturn(Optional.of(company));

        LoginResponseDTO result = authService.verifyRecovery("ana@email.com", "123456");

        assertThat(result.user().id()).isEqualTo(companyId);
    }

    @Test
    void updatePasswordComSessaoDeRecuperacaoDelegaAoSupabase() {
        when(authenticatedUserProvider.isRecoverySession()).thenReturn(true);

        authService.updatePassword("acc", "nova-senha");

        verify(supabaseAuthClient).updatePassword("acc", "nova-senha");
    }

    @Test
    void updatePasswordForaDaSessaoDeRecuperacaoLanca403SemChamarOSupabase() {
        when(authenticatedUserProvider.isRecoverySession()).thenReturn(false);

        assertThatThrownBy(() -> authService.updatePassword("acc", "nova-senha"))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode().value())
                .isEqualTo(HttpStatus.FORBIDDEN.value()));

        verifyNoInteractions(supabaseAuthClient);
    }
}
