package com.ages.pie.application.service;

import com.ages.pie.application.dto.user.UserRequestDTO;
import com.ages.pie.application.dto.user.UserResponseDTO;
import com.ages.pie.application.dto.user.UserUpdateDTO;
import com.ages.pie.application.exception.BusinessException;
import com.ages.pie.application.exception.ResourceNotFoundException;
import com.ages.pie.application.mapper.UserMapper;
import com.ages.pie.domain.entity.User;
import com.ages.pie.infrastructure.auth.SupabaseAuthClient;
import com.ages.pie.infrastructure.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final SupabaseAuthClient supabaseAuthClient;

    public UserService(UserRepository userRepository, UserMapper userMapper,
                       SupabaseAuthClient supabaseAuthClient) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.supabaseAuthClient = supabaseAuthClient;
    }

    @Transactional
    public UserResponseDTO create(UserRequestDTO dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new BusinessException("Email já cadastrado: " + dto.email());
        }

        UUID supabaseUserId = supabaseAuthClient.createUser(dto.email(), dto.password());

        try {
            User user = new User(dto.name(), dto.email(), supabaseUserId);
            return userMapper.toResponseDTO(userRepository.saveAndFlush(user));
        } catch (RuntimeException e) {
            rollbackSupabaseUser(supabaseUserId, dto.email());
            throw e;
        }
    }

    private void rollbackSupabaseUser(UUID supabaseUserId, String email) {
        try {
            supabaseAuthClient.deleteUser(supabaseUserId);
        } catch (RuntimeException e) {
            logger.error("Usuário {} ficou órfão no Supabase Auth após falha ao cadastrar {}",
                    supabaseUserId, email, e);
        }
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> findAll() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponseDTO findById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));
        return userMapper.toResponseDTO(user);
    }

    @Transactional
    public UserResponseDTO update(UUID id, UserUpdateDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));

        user.update(dto.name(), dto.photoUrl());
        return userMapper.toResponseDTO(userRepository.save(user));
    }

    @Transactional
    public void delete(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));

        userRepository.delete(user);
        userRepository.flush();

        if (user.getSupabaseUserId() != null) {
            supabaseAuthClient.deleteUser(user.getSupabaseUserId());
        }
    }
}
