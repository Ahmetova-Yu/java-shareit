package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import static org.assertj.core.api.Assertions.assertThat;

class ItemRequestDtoJsonTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldSerialize() throws Exception {
        ItemRequestDto dto = new ItemRequestDto("Need a drill");

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"description\":\"Need a drill\"");
    }

    @Test
    void shouldDeserialize() throws Exception {
        String json = "{\"description\":\"Need a drill\"}";

        ItemRequestDto dto = objectMapper.readValue(json, ItemRequestDto.class);

        assertThat(dto.getDescription()).isEqualTo("Need a drill");
    }
}