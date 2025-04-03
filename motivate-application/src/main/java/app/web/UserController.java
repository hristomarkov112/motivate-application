package app.web;

import app.security.AuthenticationMetaData;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.UserEditRequest;
import app.web.mapper.DtoMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.nio.file.AccessDeniedException;
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

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin-panel")
    public ModelAndView getAllUsers(@AuthenticationPrincipal AuthenticationMetaData authenticationMetaData) throws AccessDeniedException {

        List<User> users = userService.getAllUsers();

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("users");
        modelAndView.addObject("users", users);

        return modelAndView;
    }

    @GetMapping("/{id}/profile")
    public ModelAndView getProfilePage(@AuthenticationPrincipal AuthenticationMetaData authenticationMetaData) {

        UUID userId = authenticationMetaData.getId();
        User user = userService.getById(authenticationMetaData.getId());

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
    public ModelAndView updateUserProfile(@PathVariable UUID id, @Valid @ModelAttribute UserEditRequest userEditRequest, BindingResult bindingResult) {


            User user = userService.getById(id);
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("profile-menu");
            modelAndView.addObject("user", user);

        if (bindingResult.hasErrors()) {
            modelAndView.addObject("userEditRequest", userEditRequest);

            return modelAndView;
        }

        userService.editUserDetails(id, userEditRequest);

        return new ModelAndView("redirect:/users/{id}/profile");
    }

    @PutMapping("/{id}/status")
    public ModelAndView blockUser(@PathVariable UUID id) {

        userService.blockUser(id);

        return new ModelAndView("redirect:/users/admin-panel");
    }

    @PutMapping("/{id}/role")
    public ModelAndView changeUserRole(@PathVariable UUID id) {

        userService.changeRole(id);

        return new ModelAndView("redirect:/users/admin-panel");
    }

    @GetMapping("/profiles")
    public ModelAndView getProfilesPage(@AuthenticationPrincipal AuthenticationMetaData authenticationMetaData) {

        User user = userService.getById(authenticationMetaData.getId());

        List<User> users = userService.getRegularUsers();

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("profiles");
        modelAndView.addObject("users", users);

        return modelAndView;
    }

    @GetMapping("/other-profile")
    public ModelAndView getOtherProfilePage(@AuthenticationPrincipal AuthenticationMetaData authenticationMetaData) {

        User user = userService.getById(authenticationMetaData.getId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("other-profile");
        modelAndView.addObject("user", user);

        return modelAndView;
    }
}
