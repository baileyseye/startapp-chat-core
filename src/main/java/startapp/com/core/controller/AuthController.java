package startapp.com.core.controller;

import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import startapp.com.core.dto.AuthRequestDto;
import startapp.com.core.dto.UserDto;
import startapp.com.core.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String USER_ID = "userId";

    private final AuthService authService;
    private final HttpSession session;

    /**
     * POST {@code http://localhost:8080/api/auth/login}
     * <br><br>
     * Body:
     * <pre>{@code
     * {
     *   "username": "admin",
     *   "password": "admin"
     * }
     * }</pre>
     */
    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody AuthRequestDto request) {
        UserDto user = authService.login(request.getUsername(), request.getPassword());
        session.setAttribute(USER_ID, user.getId());

        return ok(user);
    }

    /**
     * POST {@code http://localhost:8080/api/auth/register}
     * <br><br>
     * Body:
     * <pre>{@code
     * {
     *   "username": "newuser",
     *   "password": "password123"
     * }
     * }</pre>
     */
    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody AuthRequestDto request) {
        UserDto user = authService.register(request);
        session.setAttribute(USER_ID, user.getId());

        return ok(user);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        Long userId = (Long) session.getAttribute(USER_ID);

        if (userId == null) {
            return notFound().build();
        }

        return ok(authService.getCurrentUser(userId));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        session.invalidate();
        return ok().build();
    }
}
