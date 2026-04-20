package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto create(UserDto user);
    UserDto update(Long id, UserDto user);
    UserDto getById(Long id);
    List<UserDto> getAll();
    void delete(Long id);
}
