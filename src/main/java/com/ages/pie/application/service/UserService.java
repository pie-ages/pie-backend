package com.ages.pie.application.service;

import com.ages.pie.application.dto.UserRequestDTO;
import com.ages.pie.application.dto.UserResponseDTO;
import com.ages.pie.application.dto.UserUpdateDTO;
import com.ages.pie.application.mapper.UserMapper;
import com.ages.pie.domain.entity.User;
import com.ages.pie.infrastructure.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Transactional
    public UserResponseDTO criar(UserRequestDTO dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new DataIntegrityViolationException("Email já cadastrado: " + dto.email());
        }

        User user = new User(dto.name(), dto.email(), hashSenha(dto.password()));
        return userMapper.toResponseDTO(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> listar() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponseDTO buscarPorId(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + id));
        return userMapper.toResponseDTO(user);
    }

    @Transactional
    public UserResponseDTO atualizar(UUID id, UserUpdateDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + id));

        user.update(dto.name(), dto.photoUrl());
        return userMapper.toResponseDTO(userRepository.save(user));
    }

    @Transactional
    public void deletar(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("Usuário não encontrado: " + id);
        }
        userRepository.deleteById(id);
    }

    private String hashSenha(String senha) {
        // TODO: substituir por BCryptPasswordEncoder quando Spring Security for adicionado (PIE-auth)
        return "hash(" + senha + ")";
    }
}
