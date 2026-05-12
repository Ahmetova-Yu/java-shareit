package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceTest {

    @Mock
    private ItemRequestRepository requestRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ItemRequestServiceImpl requestService;

    private User user;
    private ItemRequest request;
    private ItemRequestDto requestDto;

    @BeforeEach
    void setUp() {
        user = new User(1L, "John", "john@example.com");
        request = new ItemRequest(1L, "Need a drill", user, LocalDateTime.now());
        requestDto = new ItemRequestDto("Need a drill");
    }

    @Test
    void shouldCreateRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.save(any(ItemRequest.class))).thenReturn(request);

        var result = requestService.create(1L, requestDto);

        assertThat(result).isNotNull();
        assertThat(result.getDescription()).isEqualTo("Need a drill");
        verify(requestRepository).save(any(ItemRequest.class));
    }

    @Test
    void shouldGetUserRequests() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.findByRequestorIdOrderByCreatedDesc(1L)).thenReturn(List.of(request));

        var result = requestService.getUserRequests(1L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getDescription()).isEqualTo("Need a drill");
    }
}