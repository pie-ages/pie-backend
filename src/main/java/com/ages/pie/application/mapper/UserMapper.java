package com.ages.pie.application.mapper;

import com.ages.pie.application.dto.user.UserResponseDTO;
import com.ages.pie.domain.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponseDTO toResponseDTO(User user) {
        return new UserResponseDTO(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getPhotoUrl(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
