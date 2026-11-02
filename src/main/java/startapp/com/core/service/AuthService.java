package startapp.com.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import startapp.com.core.dto.AuthRequestDto;
import startapp.com.core.dto.UserDto;
import startapp.com.core.exception.InvalidPasswordException;
import startapp.com.core.exception.UserNotFoundException;
import startapp.com.core.exception.UsernameAlreadyExistsException;
import startapp.com.core.mapper.UserMapper;
import startapp.com.core.model.User;
import startapp.com.core.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String ADMIN = "admin";

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Аутентификация пользователя по имени и паролю.
     * <p>
     * Логика проверки пароля:
     * <ul>
     *   <li>Для пользователя {@code admin} пароль должен быть {@code admin}</li>
     *   <li>Для остальных пользователей пароль не проверяется (возраст = 0)</li>
     * </ul>
     *
     * @param username имя пользователя
     * @param password пароль
     * @return DTO аутентифицированного пользователя
     * @throws UserNotFoundException    если пользователь не найден
     * @throws InvalidPasswordException если неверный пароль для admin
     */
    @Transactional(readOnly = true)
    public UserDto login(String username, String password) {
        User user = userRepository.findByName(username)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

        if (ADMIN.equals(username) && !ADMIN.equals(password)) {
            throw new InvalidPasswordException("Неверный пароль");
        }

        return userMapper.toDto(user);
    }

    @Transactional
    public UserDto register(AuthRequestDto authRequestDto) {
        if (userRepository.findByName(authRequestDto.getUsername()).isPresent()) {
            throw new UsernameAlreadyExistsException("Username already exists");
        }

        User user = User.builder()
            .name(authRequestDto.getUsername())
            .age(0) // TODO: Добавить возраст
            .build();

        return userMapper.toDto(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserDto getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        return userMapper.toDto(user);
    }
}
