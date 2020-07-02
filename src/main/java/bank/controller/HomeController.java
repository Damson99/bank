package bank.controller;

import bank.Validator.PasswordValidator;
import bank.config.SecurityConfig;
import bank.model.EmailBody;
import bank.model.PasswordVerificationToken;
import bank.model.SenderEvent;
import bank.model.User;
import bank.service.PasswordVerificationTokenService;
import bank.service.UserService;
import bank.utils.ActualDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
@RequestMapping("/home")
public class HomeController
{
    @Autowired
    private LoginController loginController;

    @Autowired
    private UserService userService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ActualDate actualDate;

    @Autowired
    private PasswordValidator passwordValidator;

    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private PasswordVerificationTokenService passwordVerificationTokenService;

    @Value("${password.confirmation.link}")
    private String passwordConfirmationLink;


    @GetMapping
    public ModelAndView home(WebRequest webRequest)
    {
        ModelAndView modelAndView = new ModelAndView();
        User user = userService.getUserWithAuthentication();
        user.setAccountNumber(userService.accountNumberForPage(user.getAccountNumber()));

        modelAndView.addObject("user", user);
        modelAndView.addObject("date", actualDate.getTime());
        modelAndView.addObject("template", "home");
        modelAndView.addObject("pageTitle", messageSource.getMessage("home.page", null, webRequest.getLocale()));
        modelAndView.setViewName("main");
        return modelAndView;
    }

    @GetMapping("/account")
    public ModelAndView account(WebRequest webRequest)
    {
        ModelAndView modelAndView = new ModelAndView();
        User user = userService.getUserWithAuthentication();
        user.setAccountNumber(userService.accountNumberForPage(user.getAccountNumber()));

        modelAndView.addObject("user", user);
        modelAndView.addObject("date", actualDate.getTime());
        modelAndView.addObject("template", "account");
        modelAndView.addObject("pageTitle", messageSource.getMessage("account.page",
                null, webRequest.getLocale()));
        modelAndView.addObject("passFragment" , false);
        modelAndView.setViewName("main");
        return modelAndView;
    }

    @PostMapping("/{user}")
    public ModelAndView changePassword(@PathVariable("user") User user, HttpServletRequest request,
                                       RedirectAttributes redirectAttributes)
    {
        Locale locale = request.getLocale();
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("redirect:/home/account?lang=" + locale.getLanguage());

        loginController.changePasswordFunction(user, locale, redirectAttributes, modelAndView, request, passwordConfirmationLink);
        return modelAndView;
    }

    @GetMapping("/changePasswordForm")
    public ModelAndView changePasswordForm(@RequestParam("token") String token, WebRequest webRequest)
    {
        ModelAndView modelAndView = new ModelAndView();
        Locale locale = webRequest.getLocale();
        modelAndView.setViewName("redirect:/home/account?lang=" + locale.getLanguage());
        User user = new User.UserBuilder().build();

        loginController.changePasswordFormFunction(token, locale, modelAndView, user);

        modelAndView.addObject("user", user);
        modelAndView.addObject("date", actualDate.getTime());
        modelAndView.addObject("template", "account");
        modelAndView.addObject("pageTitle", messageSource.getMessage("account.page",
                null, webRequest.getLocale()));
        modelAndView.addObject("passFragment" , true);
        modelAndView.setViewName("main");
        return modelAndView;
    }

    @PostMapping("/changePasswordForm")
    public ModelAndView changePasswordForm(@RequestParam("newPassword") String newPassword,
            @RequestParam("confirmNewPassword") String confirmNewPassword, WebRequest webRequest)
    {
        ModelAndView modelAndView = new ModelAndView();
        Locale locale = webRequest.getLocale();
        User user = userService.getUserWithAuthentication();
        List<String> errors = new ArrayList<>();

        loginController.validChangePasswordForm(newPassword, confirmNewPassword, locale, modelAndView, errors, user);

        if(errors.isEmpty())
        {
            RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
            redirectAttributes.addFlashAttribute("message",
                    messageSource.getMessage("transfer.defined.success", null, locale));
            modelAndView.setViewName("redirect:/home/account?lang=" + locale.getLanguage());
        }
        else
        {
            modelAndView.addObject("user", user);
            modelAndView.addObject("date", actualDate.getTime());
            modelAndView.addObject("template", "account");
            modelAndView.addObject("pageTitle", messageSource.getMessage("account.page", null, locale));
            modelAndView.addObject("passFragment" , true);
            modelAndView.setViewName("main");
        }

        return modelAndView;
    }
}
