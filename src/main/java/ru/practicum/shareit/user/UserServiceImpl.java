package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto create(UserDto userDto) {
        User user = userMapper.toEntity(userDto);
        User savedUser = userRepository.save(user);

        return userMapper.toDto(savedUser);
    }

    @Override
    public UserDto update(Long id, UserDto userDto) {
        User existingUser = userRepository.findById(id).
                orElseThrow(() -> new RuntimeException("Не найден пользователь с id: " + id));

        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
        }

        if (userDto.getEmail() != null) {
            existingUser.setEmail(userDto.getEmail());
        }

        User updatedUser = userRepository.save(existingUser);
        return userMapper.toDto(updatedUser);
    }

    @Override
    public UserDto getById(Long id) {
        User existingUser = userRepository.findById(id).
                orElseThrow(() -> new RuntimeException("Не найден пользователь с id: " + id));

        return userMapper.toDto(existingUser);
    }

    @Override
    public List<UserDto> getAll() {
        return userRepository.findAll().values().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Не найден пользователь с id: " + id);
        }

        userRepository.deleteById(id);
    }
}
