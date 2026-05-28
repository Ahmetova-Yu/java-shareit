package ru.practicum.shareit.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.UserService;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemRequestIntegrationTest {

    @Autowired
    private ItemRequestService requestService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemRequestRepository requestRepository;

    private Long userId;

    @BeforeEach
    void setUp() {
        UserDto user = userService.create(new UserDto(null, "Requester", "requester@test.com"));
        userId = user.getId();
    }

    @Test
    void shouldCreateAndFindRequest() {
        ItemRequestDto requestDto = new ItemRequestDto("Need a drill");
        var saved = requestService.create(userId, requestDto);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getDescription()).isEqualTo("Need a drill");

        var found = requestRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getDescription()).isEqualTo("Need a drill");
    }

    @Test
    void shouldFindUserRequests() {
        requestService.create(userId, new ItemRequestDto("Request 1"));
        requestService.create(userId, new ItemRequestDto("Request 2"));

        var requests = requestService.getUserRequests(userId);

        assertThat(requests).hasSize(2);
    }
}