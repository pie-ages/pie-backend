package com.ages.pie.application.service;

import java.util.UUID;

import com.ages.pie.application.dto.LoginRequestDTO;
import com.ages.pie.application.dto.LoginResponseDTO;
import com.ages.pie.application.dto.UserSummaryDTO;
import com.ages.pie.domain.entity.Company;
import com.ages.pie.domain.entity.User;
import com.ages.pie.infrastructure.auth.SupabaseAuthClient;
import com.ages.pie.infrastructure.repository.CompanyRepository;
import com.ages.pie.infrastructure.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final SupabaseAuthClient supabaseAuthClient;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    public AuthService(SupabaseAuthClient supabaseAuthClient, UserRepository userRepository,
            CompanyRepository companyRepository) {
        this.supabaseAuthClient = supabaseAuthClient;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
    }

    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO request) {
        return toResponse(supabaseAuthClient.passwordGrant(request.email(), request.password()));
    }

    @Transactional(readOnly = true)
    public LoginResponseDTO refresh(String refreshToken) {
        return toResponse(supabaseAuthClient.refresh(refreshToken));
    }

    public void logout(String accessToken) {
        supabaseAuthClient.logout(accessToken);
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
