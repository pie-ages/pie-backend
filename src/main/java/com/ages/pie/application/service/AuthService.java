package com.ages.pie.application.service;

import com.ages.pie.application.dto.LoginRequestDTO;
import com.ages.pie.application.dto.LoginResponseDTO;
import com.ages.pie.application.dto.UserSummaryDTO;
import com.ages.pie.application.exception.NoUsersAvailableException;
import com.ages.pie.domain.entity.User;
import com.ages.pie.infrastructure.repository.UserRepository;
import com.ages.pie.infrastructure.security.JwtTokenProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Autenticação provisória de desenvolvimento (PIE-12): não valida as
 * credenciais recebidas, apenas emite um JWT para o primeiro usuário
 * cadastrado. A validação real de e-mail e senha entra na US23,
 * substituindo o corpo de {@link #login}.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findFirstByOrderByCreatedAtAsc()
            .orElseThrow(() -> new NoUsersAvailableException("Nenhum usuário cadastrado"));

        String token = jwtTokenProvider.gerarToken(user.getId());
        UserSummaryDTO summary = new UserSummaryDTO(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getPhotoUrl()
        );
        return new LoginResponseDTO(token, summary);
    }
}
