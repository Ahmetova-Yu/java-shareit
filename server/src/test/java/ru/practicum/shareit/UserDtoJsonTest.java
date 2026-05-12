package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.user.dto.UserDto;
import java.io.IOException;
import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserDtoJsonTest {

    @Autowired
    private JacksonTester<UserDto> json;

    @Test
    void shouldSerialize() throws IOException {
        UserDto dto = new UserDto();
        dto.setId(1L);
        dto.setName("John Doe");
        dto.setEmail("john@example.com");

        var result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("John Doe");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("john@example.com");
    }

    @Test
    void shouldDeserialize() throws IOException {
        String content = "{\"id\":1,\"name\":\"John Doe\",\"email\":\"john@example.com\"}";

        var result = json.parse(content);

        assertThat(result.getObject().getId()).isEqualTo(1L);
        assertThat(result.getObject().getName()).isEqualTo("John Doe");
        assertThat(result.getObject().getEmail()).isEqualTo("john@example.com");
    }
}