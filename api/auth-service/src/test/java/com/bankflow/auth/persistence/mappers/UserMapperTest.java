package com.bankflow.auth.persistence.mappers;

import com.bankflow.auth.domain.models.User;
import com.bankflow.auth.persistence.jpa.UserJpaEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper mapper = new UserMapper();

    @Test
    void toDomainKeepsValuesFromDatabase() {
        LocalDateTime createdLongAgo = LocalDateTime.of(2020, 1, 1, 12, 0);

        UserJpaEntity model = new UserJpaEntity();
        model.setId(UUID.randomUUID());
        model.setEmail("nikola@test.com");
        model.setPassword("hash");
        model.setFirstName("Nikola");
        model.setLastName("Zivkovic");
        model.setActive(false);
        model.setCreatedAt(createdLongAgo);
        model.setUpdatedAt(createdLongAgo);

        User user = mapper.toDomain(model);

        assertThat(user.isActive()).isFalse();
        assertThat(user.getCreatedAt()).isEqualTo(createdLongAgo);
    }
}