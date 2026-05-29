package com.bankflow.auth.infra.mappers;

import com.bankflow.auth.core.entities.User;
import com.bankflow.auth.infra.models.UserModel;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserModel toModel(User user) {
        UserModel model = new UserModel();
        model.setEmail(user.getEmail());
        model.setPassword(user.getPassword());
        model.setFirstName(user.getFirstName());
        model.setLastName(user.getLastName());
        model.setActive(user.isActive());
        model.setCreatedAt(user.getCreatedAt());
        model.setUpdatedAt(user.getUpdatedAt());
        return model;
    }

    public User toDomain(UserModel model) {
        return new User(
                model.getId(),
                model.getEmail(),
                model.getPassword(),
                model.getFirstName(),
                model.getLastName()
        );
    }
}