package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;
import static org.assertj.core.api.Assertions.assertThat;

class ItemDtoJsonTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldSerialize() throws Exception {
        ItemDto dto = new ItemDto(1L, "Drill", "Powerful drill", true, null);

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"name\":\"Drill\"");
        assertThat(json).contains("\"available\":true");
    }

    @Test
    void shouldDeserialize() throws Exception {
        String json = "{\"id\":1,\"name\":\"Drill\",\"description\":\"Powerful drill\",\"available\":true}";

        ItemDto dto = objectMapper.readValue(json, ItemDto.class);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Drill");
        assertThat(dto.getAvailable()).isTrue();
    }
}