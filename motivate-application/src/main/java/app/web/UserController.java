package app.web;

import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.UserEditRequest;
import app.web.mapper.DtoMapper;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView getAllUsers() {

        List<User> users = userService.getAllUsers();

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("users");
        modelAndView.addObject("users", users);

        return modelAndView;
    }

    @GetMapping("/{id}/profile")
    public ModelAndView getProfilePage(HttpSession session) {

        UUID userId = (UUID) session.getAttribute("user_id");
        User user = userService.getById(userId);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("profile");
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @GetMapping("/{id}/profile-menu")
    public ModelAndView gerProfileMenu(@PathVariable UUID id, @Valid UserEditRequest userEditRequest) {

        User user = userService.getById(id);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("profile-menu");
        modelAndView.addObject("user", user);
        modelAndView.addObject("userEditRequest", DtoMapper.mapUserToEditRequest(user));

        return modelAndView;
    }

    @PutMapping("/{id}/profile-menu")
    public ModelAndView updateUserProfile(@PathVariable UUID id, @Valid UserEditRequest userEditRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            User user = userService.getById(id);
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("profile-menu");
            modelAndView.addObject("user", user);
            modelAndView.addObject("userEditRequest", userEditRequest);

            return modelAndView;
        }

        userService.editUserDetails(id, userEditRequest);

        return new ModelAndView("redirect:/home");
    }

    @PutMapping("/{id}/block")
    public ModelAndView blockUser(@PathVariable UUID id) {

        userService.blockUser(id);

        return new ModelAndView("redirect:/users");
    }

    @PutMapping("/{id}/role")
    public ModelAndView changeUserRole(@PathVariable UUID id) {

        userService.changeRole(id);

        return new ModelAndView("redirect:/users");
    }
}
