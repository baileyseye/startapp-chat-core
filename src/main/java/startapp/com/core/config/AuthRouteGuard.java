package startapp.com.core.config;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Простой guard для проверки авторизации
 */
@Component
public class AuthRouteGuard {

    public boolean isAuthorized() {
        HttpSession session = getSession();
        return session != null && session.getAttribute("userId") != null;
    }

    private HttpSession getSession() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                return attributes.getRequest().getSession(false);
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }
}
