package app.web;

import app.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.file.AccessDeniedException;

@ControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler(PostHasNoOwnerException.class)
    public String handlePostHasNoOwnerException(RedirectAttributes redirectAttributes, PostHasNoOwnerException exception) {

        String message = exception.getMessage();

        redirectAttributes.addFlashAttribute("postHasNoOwnerExceptionMessage", message);
        return "redirect:/posts";
    }

    @ExceptionHandler(PostNotFoundException.class)
    public String handlePostNotFoundException(RedirectAttributes redirectAttributes, PostNotFoundException exception) {

        String message = exception.getMessage();

        redirectAttributes.addFlashAttribute("uPostNotFoundExceptionMessage", message);
        return "redirect:/posts";
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public String handleUsernameAlreadyExistsException(RedirectAttributes redirectAttributes, UsernameAlreadyExistsException exception) {

        String message = exception.getMessage();

        redirectAttributes.addFlashAttribute("usernameAlreadyExistsMessage", message);
        return "redirect:/register";
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public String handleUsernameNotFoundExceptionException(RedirectAttributes redirectAttributes, UsernameNotFoundException exception) {

        String message = exception.getMessage();

        redirectAttributes.addFlashAttribute("usernameNotFoundExceptionMessage", message);
        return "redirect:/register";
    }

    @ExceptionHandler(UsernameDoesNotExistException.class)
    public String handleUsernameDoesNotExistException(RedirectAttributes redirectAttributes, UsernameDoesNotExistException exception) {

        String message = exception.getMessage();

        redirectAttributes.addFlashAttribute("usernameDoesNotExistExceptionMessage", message);
        return "redirect:/register";
    }

    @ExceptionHandler(NonExistingUsernameException.class)
    public String handleNonExistingUsernameException(RedirectAttributes redirectAttributes, NonExistingUsernameException exception) {

        String message = exception.getMessage();

        redirectAttributes.addFlashAttribute("nonExistingUsernameExceptionMessage", message);
        return "redirect:/register";
    }

    @ExceptionHandler(UnauthorizedPostAccessException.class)
    public String handleUnauthorizedPostAccessException(RedirectAttributes redirectAttributes, UnauthorizedPostAccessException exception) {

        String message = exception.getMessage();

        redirectAttributes.addFlashAttribute("unauthorizedPostAccessExceptionMessage", message);
        return "redirect:/posts";
    }

    @ExceptionHandler(WalletNotFoundException.class)
    public String handleWalletNotFoundExceptionException(RedirectAttributes redirectAttributes, WalletNotFoundException exception) {

        String message = exception.getMessage();

        redirectAttributes.addFlashAttribute("walletNotFoundExceptionMessage", message);
        return "redirect:/payments";
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public String handleInsufficientFundsException(RedirectAttributes redirectAttributes, InsufficientFundsException exception) {

        String message = exception.getMessage();

        redirectAttributes.addFlashAttribute("insufficientFundsException", message);
        return "redirect:/payments";
    }

    @ExceptionHandler(NoActiveMembershipException.class)
    public String handleNoActiveMembershipException(RedirectAttributes redirectAttributes, NoActiveMembershipException exception) {

        String message = exception.getMessage();

        redirectAttributes.addFlashAttribute("noActiveMembershipExceptionMessage", message);
        return "redirect:/memberships";
    }

//    @ResponseStatus(HttpStatus.NOT_FOUND)
//    @ExceptionHandler({
//            AccessDeniedException.class,
//            NoResourceFoundException.class,
//            MethodArgumentTypeMismatchException.class,
//            MissingRequestValueException.class,
//    })
//
//    public ModelAndView handleNotFoundExceptions(Exception exception) {
//
//        return new ModelAndView("not-found");
//    }
//
//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//    @ExceptionHandler(Exception.class)
//    public ModelAndView handleAnyException(Exception exception) {
//
//        ModelAndView modelAndView = new ModelAndView();
//        modelAndView.setViewName("internal-server-error");
//        modelAndView.addObject("errorMessage", exception.getClass().getSimpleName());
//
//        return modelAndView;
//    }
}


