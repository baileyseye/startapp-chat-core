package startapp.com.core.controller;

import static java.net.URI.create;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import startapp.com.core.dto.UserDto;
import startapp.com.core.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    /**
     * GET {@code http://localhost:8080/api/users}
     * <br>
     * Пример: {@code http://localhost:8080/api/users}
     */
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ok(userService.getAllUsers());
    }

    /**
     * GET {@code http://localhost:8080/api/users/{id}}
     * <br>
     * Пример: {@code http://localhost:8080/api/users/1}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return ok(userService.getUserById(id));
    }

    /**
     * GET {@code http://localhost:8080/api/users/search}
     * <br><br>
     * Пример: {@code http://localhost:8080/api/users/search?query=иван}
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserDto>> searchUsers(@RequestParam String query) {
        return ok(userService.searchUsers(query));
    }

    /**
     * POST {@code http://localhost:8080/api/users}
     * <br><br>
     * Body:
     * <pre>{@code
     * {
     *   "name": "Иван Иванов",
     *   "age": 25
     * }
     * }</pre>
     */
    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
        UserDto createdUser = userService.createUser(userDto);
        return created(create("/api/users/" + createdUser.getId())).body(createdUser);
    }

    /**
     * PUT {@code http://localhost:8080/api/users/{id}}
     * <br><br>
     * Body:
     * <pre>{@code
     * {
     *   "name": "Иван Петров",
     *   "age": 26
     * }
     * }</pre>
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        return ok(userService.updateUser(id, userDto));
    }

    /**
     * DELETE {@code http://localhost:8080/api/users/{id}}
     * <br>
     * Пример: {@code http://localhost:8080/api/users/1}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return noContent().build();
    }
}
