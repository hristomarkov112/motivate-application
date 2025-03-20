package app.web;

import app.additionalinfo.client.dto.AdditionalInfo;
import app.additionalinfo.client.dto.UpsertAdditionalInfo;
import app.additionalinfo.service.AdditionalInfoService;
import app.security.AuthenticationMetaData;
import app.user.model.User;
import app.user.service.UserService;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("/additional-info")
public class AdditionalInfoController {

    private final AdditionalInfoService additionalInfoService;
    private final UserService userService;

    @Autowired
    public AdditionalInfoController(AdditionalInfoService additionalInfoService, UserService userService) {
        this.additionalInfoService = additionalInfoService;
        this.userService = userService;
    }

    @GetMapping()
    public ModelAndView getAdditionalInfoPage(@AuthenticationPrincipal AuthenticationMetaData authenticationMetaData) {
        User user = userService.getById(authenticationMetaData.getId());

        AdditionalInfo additionalInfo = additionalInfoService.getAdditionalInfo(user.getId());

        ModelAndView modelAndView = new ModelAndView("additional-info");
        modelAndView.addObject("user", user);
        modelAndView.addObject("additionalInfo", additionalInfo);

        return modelAndView;
    }

    @GetMapping("/{id}/form")
    public ModelAndView getProfileMenu(@PathVariable UUID id, AdditionalInfo additionalInfo) {

        User user = userService.getById(id);

        ModelAndView modelAndView = new ModelAndView("additional-info-menu");
        modelAndView.addObject("user", user);
        modelAndView.addObject("additionalInfo", additionalInfo);

        return modelAndView;
    }
}
