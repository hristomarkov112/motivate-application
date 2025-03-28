package app.web;

import app.additionalinfo.client.dto.AdditionalInfo;
import app.additionalinfo.service.AdditionalInfoService;
import app.security.AuthenticationMetaData;
import app.user.model.User;
import app.user.service.UserService;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
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

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("additional-info");
        modelAndView.addObject("user", user);
        modelAndView.addObject("additionalInfo", additionalInfo);

        return modelAndView;
    }

    @GetMapping("/{id}/form")
    public ModelAndView getProfileMenu(@PathVariable UUID id, @AuthenticationPrincipal AuthenticationMetaData authenticationMetaData) {

        User user = userService.getById(authenticationMetaData.getId());
        UUID userId = user.getId();

        AdditionalInfo additionalInfo = additionalInfoService.getAdditionalInfo(userId);

        ModelAndView modelAndView = new ModelAndView("additional-info-menu");
        modelAndView.addObject("user", user);
        modelAndView.addObject("additionalInfo", additionalInfo);

        return modelAndView;
    }

    @PostMapping("/{id}/form")
    public ModelAndView submitAdditionalInfoForm(@PathVariable UUID id,@ModelAttribute AdditionalInfo additionalInfo, @AuthenticationPrincipal AuthenticationMetaData authenticationMetaData) {

        User user = userService.getById(authenticationMetaData.getId());

//        try {
            additionalInfoService.saveAdditionalInfo(id, additionalInfo.getGender(), additionalInfo.getPhoneNumber(), additionalInfo.getSecondEmail());
            ModelAndView modelAndView = new ModelAndView("redirect:/additional-info");
            modelAndView.addObject("additional-info", additionalInfo);

            return modelAndView;
//        }
//        catch (FeignException.BadRequest e) {
//            ModelAndView modelAndView = new ModelAndView("additional-info-menu");
//            modelAndView.addObject("user", user);
//            modelAndView.addObject("additional-info", additionalInfo);
//            modelAndView.addObject("error", "Invalid input. Please check your data.");
//            return modelAndView;
//        } catch (Exception e) {
//            ModelAndView modelAndView = new ModelAndView("additional-info-menu");
//            modelAndView.addObject("user", user);
//            modelAndView.addObject("additional-info", additionalInfo);
//            modelAndView.addObject("error", "An error occurred while saving additional information.");
//            return modelAndView;
//        }
    }
}
