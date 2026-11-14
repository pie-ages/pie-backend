package com.ages.pie.application.service;

import java.util.UUID;

import com.ages.pie.application.dto.LoginRequestDTO;
import com.ages.pie.application.dto.LoginResponseDTO;
import com.ages.pie.application.dto.UserSummaryDTO;
import com.ages.pie.application.exception.InvalidCredentialsException;
import com.ages.pie.domain.entity.Company;
import com.ages.pie.domain.entity.User;
import com.ages.pie.infrastructure.auth.SupabaseAuthClient;
import com.ages.pie.infrastructure.repository.CompanyRepository;
import com.ages.pie.infrastructure.repository.UserRepository;
import com.ages.pie.infrastructure.security.AuthenticatedUserProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final SupabaseAuthClient supabaseAuthClient;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public AuthService(SupabaseAuthClient supabaseAuthClient, UserRepository userRepository,
            CompanyRepository companyRepository, AuthenticatedUserProvider authenticatedUserProvider) {
        this.supabaseAuthClient = supabaseAuthClient;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO request) {
        return toResponse(supabaseAuthClient.passwordGrant(request.email(), request.password()));
    }

    @Transactional(readOnly = true)
    public LoginResponseDTO loginCustomer(LoginRequestDTO request) {
        LoginResponseDTO response = login(request);
        if (response.user() != null && response.user().role() != null) {
            throw new InvalidCredentialsException("Credenciais inválidas");
        }
        return response;
    }

    @Transactional
    public LoginResponseDTO loginCompany(LoginRequestDTO request) {
        SupabaseAuthClient.Session session = supabaseAuthClient.passwordGrant(request.email(), request.password());
        UserSummaryDTO user = findSummaryBySupabaseUserId(session.user().id());
        if (user == null) {
            user = companyRepository.findByEmail(request.email())
                .map(c -> {
                    c.linkSupabaseUser(session.user().id());
                    companyRepository.save(c);
                    return toSummary(c);
                })
                .orElse(null);
        }
        if (user == null || user.role() == null) {
            throw new InvalidCredentialsException("Credenciais inválidas");
        }
        return new LoginResponseDTO(session.accessToken(), session.refreshToken(),
            session.expiresIn(), session.tokenType(), user);
    }

    @Transactional(readOnly = true)
    public LoginResponseDTO refresh(String refreshToken) {
        return toResponse(supabaseAuthClient.refresh(refreshToken));
    }

    public void logout(String accessToken) {
        supabaseAuthClient.logout(accessToken);
    }

    public void recover(String email) {
        supabaseAuthClient.recover(email);
    }

    @Transactional(readOnly = true)
    public LoginResponseDTO verifyRecovery(String email, String code) {
        return toResponse(supabaseAuthClient.verifyRecovery(email, code));
    }

    public void updatePassword(String accessToken, String password) {
        requireRecoverySession();
        supabaseAuthClient.updatePassword(accessToken, password);
    }

    @Transactional(readOnly = true)
    public UserSummaryDTO findSummary(UUID userId) {
        return userRepository.findById(userId)
            .map(AuthService::toSummary)
            .or(() -> companyRepository.findById(userId).map(AuthService::toSummary))
            .orElse(null);
    }

    private UserSummaryDTO findSummaryBySupabaseUserId(UUID supabaseUserId) {
        return userRepository.findBySupabaseUserId(supabaseUserId)
            .map(AuthService::toSummary)
            .or(() -> companyRepository.findBySupabaseUserId(supabaseUserId).map(AuthService::toSummary))
            .orElse(null);
    }

    private void requireRecoverySession() {
        if (!authenticatedUserProvider.isRecoverySession()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Troca de senha disponível apenas pela recuperação de acesso");
        }
    }

    private static UserSummaryDTO toSummary(User user) {
        return new UserSummaryDTO(user.getId(), user.getName(), user.getEmail(), user.getPhotoUrl(), null, null);
    }

    private static UserSummaryDTO toSummary(Company company) {
        return new UserSummaryDTO(
            company.getId(),
            company.getName(),
            company.getEmail(),
            company.getPhotoUrl(),
            company.getRole() != null ? company.getRole().name() : null,
            company.getStatus() != null ? company.getStatus().name() : null
        );
    }

    private LoginResponseDTO toResponse(SupabaseAuthClient.Session session) {
        UserSummaryDTO user = session.user() == null ? null : findSummaryBySupabaseUserId(session.user().id());
        return new LoginResponseDTO(
            session.accessToken(),
            session.refreshToken(),
            session.expiresIn(),
            session.tokenType(),
            user
        );
    }
}
