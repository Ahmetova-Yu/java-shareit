package ru.practicum.shareit.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldCreateAndFindUser() {
        UserDto userDto = new UserDto(null, "Integration Test", "integration@test.com");
        UserDto saved = userService.create(userDto);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Integration Test");
        assertThat(saved.getEmail()).isEqualTo("integration@test.com");

        User found = userRepository.findById(saved.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getEmail()).isEqualTo("integration@test.com");
    }

    @Test
    void shouldUpdateUser() {
        UserDto userDto = new UserDto(null, "Original Name", "update@test.com");
        UserDto saved = userService.create(userDto);

        UserDto updateDto = new UserDto(saved.getId(), "Updated Name", "update@test.com");
        UserDto updated = userService.update(saved.getId(), updateDto);

        assertThat(updated.getName()).isEqualTo("Updated Name");
    }

    @Test
    void shouldDeleteUser() {
        UserDto userDto = new UserDto(null, "To Delete", "delete@test.com");
        UserDto saved = userService.create(userDto);

        userService.delete(saved.getId());

        assertThat(userRepository.findById(saved.getId())).isEmpty();
    }
}