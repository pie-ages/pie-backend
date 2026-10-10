package com.ages.pie.application.service;

import java.util.UUID;

import com.ages.pie.application.dto.LoginRequestDTO;
import com.ages.pie.application.dto.LoginResponseDTO;
import com.ages.pie.application.dto.UserSummaryDTO;
import com.ages.pie.infrastructure.auth.SupabaseAuthClient;
import com.ages.pie.infrastructure.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Autenticação delegada ao Supabase Auth. O id do usuário no Supabase
 * ({@code sub} do JWT) é o id do customer; {@code user} vem nulo enquanto
 * o customer ainda não foi cadastrado.
 */
@Service
public class AuthService {

    private final SupabaseAuthClient supabaseAuthClient;
    private final UserRepository userRepository;

    public AuthService(SupabaseAuthClient supabaseAuthClient, UserRepository userRepository) {
        this.supabaseAuthClient = supabaseAuthClient;
        this.userRepository = userRepository;
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
            .map(user -> new UserSummaryDTO(user.getId(), user.getName(), user.getEmail(), user.getPhotoUrl()))
            .orElse(null);
    }

    private LoginResponseDTO toResponse(SupabaseAuthClient.Session session) {
        UserSummaryDTO user = session.user() == null ? null : findSummary(session.user().id());
        return new LoginResponseDTO(
            session.accessToken(),
            session.refreshToken(),
            session.expiresIn(),
            session.tokenType(),
            user
        );
    }
}
