package startapp.com.core.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import startapp.com.core.dto.UserDto;
import startapp.com.core.exception.UserNotFoundException;
import startapp.com.core.mapper.UserMapper;
import startapp.com.core.model.User;
import startapp.com.core.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
            .map(userMapper::toDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        return userMapper.toDto(user);
    }

    @Transactional
    public UserDto createUser(UserDto userDto) {
        User user = userMapper.toEntity(userDto);
        User saved = userRepository.save(user);

        return userMapper.toDto(saved);
    }

    @Transactional
    public UserDto updateUser(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        user.setName(userDto.getName());
        user.setAge(userDto.getAge());

        User updated = userRepository.save(user);
        return userMapper.toDto(updated);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        }

        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<UserDto> searchUsers(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        return userRepository.findByNameContainingIgnoreCase(query.trim()).stream()
            .map(userMapper::toDto)
            .toList();
    }
}
