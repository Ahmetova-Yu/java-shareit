package ru.practicum.shareit.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.UserService;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemRepository itemRepository;

    private Long ownerId;

    @BeforeEach
    void setUp() {
        UserDto owner = userService.create(new UserDto(null, "Owner", "owner@test.com"));
        ownerId = owner.getId();
    }

    @Test
    void shouldCreateAndFindItem() {
        ItemDto itemDto = new ItemDto(null, "Drill", "Powerful drill", true, null);
        ItemDto saved = itemService.create(ownerId, itemDto);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Drill");

        Item found = itemRepository.findById(saved.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getOwner().getId()).isEqualTo(ownerId);
    }

    @Test
    void shouldFindItemsByOwner() {
        itemService.create(ownerId, new ItemDto(null, "Item1", "Desc1", true, null));
        itemService.create(ownerId, new ItemDto(null, "Item2", "Desc2", true, null));

        var items = itemService.getAllByOwner(ownerId);

        assertThat(items).hasSize(2);
    }
}