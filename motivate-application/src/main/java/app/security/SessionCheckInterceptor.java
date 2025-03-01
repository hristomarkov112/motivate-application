package app.security;

import app.user.model.User;
import app.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;
import java.util.UUID;

@Component
public class SessionCheckInterceptor implements HandlerInterceptor {

    private final Set<String> UNAUTHORIZED_URLS = Set.of("/", "/login", "/register");
    private final UserService userService;

    @Autowired
    public SessionCheckInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String endpoint = request.getServletPath();
        if (UNAUTHORIZED_URLS.contains(endpoint)) {
            return true;
        }

        HttpSession currentUserSession = request.getSession(false);
        if (currentUserSession == null) {
            response.sendRedirect("/login");
            return false;
        }

        UUID userId =(UUID) currentUserSession.getAttribute("user_id");
        User user = userService.getById(userId);

        if(user.isBlocked()) {
            currentUserSession.invalidate();
            response.sendRedirect("/");
            return false;
        }

        return true;
    }
}
