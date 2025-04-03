package app.web;

import app.exception.InsufficientFundsException;
import app.security.AuthenticationMetaData;
import app.user.model.User;
import app.user.service.UserService;
import app.wallet.service.WalletService;
import app.web.dto.DepositRequest;
import app.web.dto.WithdrawalRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.UUID;


@Controller
@RequestMapping("/wallets")
public class WalletController {

    private final UserService userService;
    private final WalletService walletService;

    @Autowired
    public WalletController(UserService userService, WalletService walletService) {
        this.userService = userService;
        this.walletService = walletService;
    }

    @GetMapping()
    public ModelAndView getWalletsPage(@AuthenticationPrincipal AuthenticationMetaData authenticationMetaData) {

        User user = userService.getById(authenticationMetaData.getId());
        UUID userId = authenticationMetaData.getId();

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("wallets");
        modelAndView.addObject("user", user);

        if (user == null) {
            throw new RuntimeException("User with ID " + userId + " not found");
        }

        return modelAndView;
    }

    @GetMapping("/deposit")
    public String showDepositForm(Model model) {
        model.addAttribute("depositRequest", new DepositRequest(new BigDecimal("0.00")));
        return "deposit-form";
    }

    @PostMapping("/deposit")
    public String processDeposit(
            @AuthenticationPrincipal AuthenticationMetaData auth,
            @ModelAttribute("depositRequest") @Valid DepositRequest depositRequest,
            Model model) {

        User user = userService.getById(auth.getId());
        walletService.deposit(user.getWallets().get(0).getId(), depositRequest.getAmount());

        model.addAttribute("depositRequest", depositRequest);

        return "deposit-result";
    }

    @GetMapping("/withdrawal")
    public String showWithdrawalForm(Model model) {
        model.addAttribute("withdrawalRequest", new WithdrawalRequest(new BigDecimal("0.00")));
        return "withdrawal-form";
    }

    @PostMapping("/withdrawal")
    public String processWithdrawal(
            @AuthenticationPrincipal AuthenticationMetaData auth,
            @ModelAttribute("withdrawalRequest") @Valid WithdrawalRequest withdrawalRequest,
            Model model, BindingResult bindingResult) {

        User user = userService.getById(auth.getId());

        if (bindingResult.hasErrors()) {
            model.addAttribute("errors", bindingResult.getAllErrors());
            return "withdrawal-form";
        }

        walletService.withdrawal(user.getWallets().get(0).getId(), withdrawalRequest.getAmount());

        model.addAttribute("withdrawalRequest", withdrawalRequest);

        return "withdrawal-result";
    }
}
