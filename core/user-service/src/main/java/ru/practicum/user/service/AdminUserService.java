package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.ValidationException;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserRepository userRepository;

    public List<UserDto> getUsersByParams(List<Long> ids, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from, size);
        return UserMapper.toUserDto(userRepository.findByIdIn(ids, pageable));
    }

    @Transactional
    public UserDto createNewUser(NewUserRequest newUserRequest) {
        checkDuplicateUserByEmail(newUserRequest);
        User user = UserMapper.toUser(newUserRequest);
        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Transactional
    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> getUsersWithIds(List<Long> ids) {
        return userRepository.findAllById(ids);
    }

    private void checkDuplicateUserByEmail(NewUserRequest user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ValidationException("Пользователь с email = " + user.getEmail() + " уже существует");
        }
    }
}
