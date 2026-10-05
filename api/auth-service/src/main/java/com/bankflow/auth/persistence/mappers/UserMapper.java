package com.bankflow.auth.persistence.mappers;

import com.bankflow.auth.domain.models.User;
import com.bankflow.auth.persistence.jpa.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserJpaEntity toModel(User user) {
        UserJpaEntity model = new UserJpaEntity();
        model.setId(user.getId());
        model.setEmail(user.getEmail());
        model.setPassword(user.getPassword());
        model.setFirstName(user.getFirstName());
        model.setLastName(user.getLastName());
        model.setActive(user.isActive());
        model.setCreatedAt(user.getCreatedAt());
        model.setUpdatedAt(user.getUpdatedAt());
        return model;
    }

    public User toDomain(UserJpaEntity model) {
        return User.restore(
                model.getId(),
                model.getEmail(),
                model.getPassword(),
                model.getFirstName(),
                model.getLastName(),
                model.isActive(),
                model.getCreatedAt(),
                model.getUpdatedAt()
        );
    }
}