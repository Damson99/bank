package bank.controller;

import bank.Validator.PasswordValidator;
import bank.config.SecurityConfig;
import bank.model.EmailBody;
import bank.model.PasswordVerificationToken;
import bank.model.SenderEvent;
import bank.model.User;
import bank.service.PasswordVerificationTokenService;
import bank.service.UserService;
import bank.service.VerificationTokenService;
import bank.utils.ActualDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class LoginController
{
    @Autowired
    private UserService userService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private VerificationTokenService verificationTokenService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private PasswordValidator passwordValidator;

    @Autowired
    private PasswordVerificationTokenService passwordVerificationTokenService;

    @Autowired
    private ActualDate actualDate;

    @Autowired
    private SecurityConfig securityConfig;

    @Value("${email.support.email}")
    private String BANK_IS_RECIPIENT;

    @Value("${login.password.confirmation.link}")
    private String loginPasswordConfirmationLink;


    @GetMapping(value = {"/", "/login"})
    public ModelAndView login(WebRequest webRequest)
    {
        ModelAndView modelAndView = new ModelAndView();

        modelAndView.addObject("pageTitle",
                messageSource.getMessage("login.page", null, webRequest.getLocale()));
        modelAndView.setViewName("login");
        return modelAndView;
    }

    @GetMapping("/registration")
    public ModelAndView registration(WebRequest webRequest)
    {
        ModelAndView modelAndView = new ModelAndView();
        User user = new User();

        modelAndView.addObject("user", user);
        modelAndView.addObject("pageTitle",
                messageSource.getMessage("register.page", null, webRequest.getLocale()));
        modelAndView.addObject("passwordDisabled", false);
        modelAndView.addObject("disabled", false);
        modelAndView.setViewName("registration");
        return modelAndView;
    }

    @PostMapping("/registration")
    public ModelAndView createNewUser(@Valid User user, BindingResult bindingResult, WebRequest webRequest,
                                      RedirectAttributes redirectAttributes)
    {
        ModelAndView modelAndView = new ModelAndView();
        Locale locale = webRequest.getLocale();
        modelAndView.addObject("passwordDisabled", false);

        checkIsExists(user.getId(), user.getEmail(), user.getPersonalIdNumber(), user.getPhoneNumber(),
                locale, bindingResult);

        if(!user.getPassword().equals(user.getConfirmPassword()))
        {
            bindingResult.rejectValue("password", "error.user",
                    messageSource.getMessage("regForm.error.similar.passwords", null, locale));
        }

        if(user.getPassword().length() > 40 || user.getPassword().length() < 8)
        {
            bindingResult.rejectValue("password", "error.user",
                    messageSource.getMessage("password.size", null, locale));
        }

        if(passwordValidator.passwordIllegalCharacters(user.getPassword()))
        {
            bindingResult.rejectValue("password", "error.user",
                    messageSource.getMessage("illegal.characters", null, locale));
        }

        if(bindingResult.hasErrors())
        {
            modelAndView.setViewName("registration");
            return modelAndView;
        }

        userService.saveUser(user);
        try
        {
            String subject = messageSource.getMessage("registration.email.subject", null, locale);
            String message = messageSource.getMessage("registration.email.message", null, locale);
            EmailBody emailBody = new EmailBody(subject, message, user.getEmail());
            eventPublisher.publishEvent(new SenderEvent(locale, emailBody));

            String supportSubject = messageSource.getMessage("support.registration.email.subject", null, locale);
            String supportMessage = messageSource.getMessage("support.registration.email.message", null, locale)
                    + user.getId();
            EmailBody supportEmailBody = new EmailBody(supportSubject, supportMessage, BANK_IS_RECIPIENT);
            eventPublisher.publishEvent(new SenderEvent(locale, supportEmailBody));
        }
        catch (Exception ex)
        {
            modelAndView.addObject("bigError",
                    messageSource.getMessage("error.activation.link", null, locale));
            return modelAndView;
        }

        redirectAttributes.addFlashAttribute("message",
                messageSource.getMessage("success.registering", null, locale));
        modelAndView.setViewName("redirect:/login");
        return modelAndView;
    }

    public void checkIsExists(Long id, String email, String personalIdNumber, String phoneNumber,
                               Locale locale, BindingResult bindingResult)
    {
        User emailExists = userService.findByUserEmail(email);
        User personalIdNumberExists = userService.findByUserPersonalIdNumber(personalIdNumber);
        User phoneNumberExists = userService.findByUserPhoneNumber(phoneNumber);

        if(emailExists != null)
        {
            if(id == null)
            {
                bindingResult.rejectValue("email", "error.user",
                        messageSource.getMessage("regForm.error.existing.email", null, locale));
            }
            else if(!id.equals(emailExists.getId()))
            {
                bindingResult.rejectValue("email", "error.user",
                        messageSource.getMessage("regForm.error.existing.email", null, locale));
            }
        }

        if(personalIdNumberExists != null)
        {
            if(id == null)
            {
                bindingResult.rejectValue("personalIdNumber", "error.user",
                        messageSource.getMessage("regForm.error.existing.personalIdNumber", null, locale));
            }
            else if(!id.equals(personalIdNumberExists.getId()))
            {
                bindingResult.rejectValue("personalIdNumber", "error.user",
                        messageSource.getMessage("regForm.error.existing.personalIdNumber", null, locale));
            }
        }

        if(phoneNumberExists != null)
        {
            if(id == null)
            {
                bindingResult.rejectValue("phoneNumber", "error.user",
                        messageSource.getMessage("regForm.error.existing.phoneNumber", null, locale));
            }
            else if(!id.equals(phoneNumberExists.getId()))
            {
                bindingResult.rejectValue("phoneNumber", "error.user",
                        messageSource.getMessage("regForm.error.existing.phoneNumber", null, locale));
            }
        }
    }

    @GetMapping("/passwordReset")
    public ModelAndView passwordReset(WebRequest webRequest)
    {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("passwordReset");
        modelAndView.addObject("pageTitle",
                messageSource.getMessage("password.reset", null, webRequest.getLocale()));
        modelAndView.addObject("passFragment" , false);
        return modelAndView;
    }

    @PostMapping("/passwordReset")
    public ModelAndView changePassword(@RequestParam("email") String email, HttpServletRequest request,
                                       RedirectAttributes redirectAttributes)
    {
        Locale locale = request.getLocale();
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("redirect:/passwordReset?lang=" + locale.getLanguage());

        if(email.equals(""))
        {
            redirectAttributes.addFlashAttribute("fatalError",
                    messageSource.getMessage("email.size", null, locale));
            return modelAndView;
        }

        User user = userService.findByUserEmail(email);
        modelAndView.setViewName("redirect:/login?lang=" + locale.getLanguage());
        modelAndView.addObject("passFragment" , false);
        return changePasswordFunction(user, locale, redirectAttributes, modelAndView, request, loginPasswordConfirmationLink);
    }

    public ModelAndView changePasswordFunction(User user, Locale locale, RedirectAttributes redirectAttributes,
                    ModelAndView modelAndView, HttpServletRequest request, String link)
    {
        String token = UUID.randomUUID().toString();
        try
        {
            String subject = messageSource.getMessage("password.subject", null, locale);
            String message = messageSource.getMessage("password.content", null, locale)
                    + " <a href=\"http://" + new URL(request.getRequestURL().toString()).getHost() + link + token + "\">"
                    + messageSource.getMessage("change.password", null, locale) + "</a>";
            EmailBody emailBody = new EmailBody(subject, message, user.getEmail());
            eventPublisher.publishEvent(new SenderEvent(locale, emailBody));

            redirectAttributes.addFlashAttribute("message",
                    messageSource.getMessage("password.link", null, locale));
        }
        catch (Exception ex)
        {
            redirectAttributes.addFlashAttribute("fatalError",
                    messageSource.getMessage("error.password.link", null, locale));
            return modelAndView;
        }

        passwordVerificationTokenService.createVerificationToken(user, token);
        return modelAndView;
    }

    @GetMapping("/passwordReset/form")
    public ModelAndView changePasswordForm(@RequestParam("token") String token, WebRequest webRequest)
    {
        ModelAndView modelAndView = new ModelAndView();
        Locale locale = webRequest.getLocale();
        modelAndView.setViewName("redirect:/login?lang=" + locale.getLanguage());
        modelAndView.addObject("passFragment" , false);
        User user = null;

        changePasswordFormFunction(token, locale, modelAndView, user);

        modelAndView.addObject("pageTitle", messageSource.getMessage("password.reset",
                null, webRequest.getLocale()));
        modelAndView.setViewName("passwordReset");
        return modelAndView;
    }

    public ModelAndView changePasswordFormFunction(String token, Locale locale, ModelAndView modelAndView, User user)
    {
        PasswordVerificationToken passwordVerificationToken = passwordVerificationTokenService.getVerificationToken(token);

        if(passwordVerificationToken == null)
        {
            modelAndView.addObject("fatalError",
                    messageSource.getMessage("auth.message.invalidToken", null, locale));
            return modelAndView;
        }

        if(passwordVerificationTokenService.expiryDate(passwordVerificationToken.getExpiryDate()))
        {
            modelAndView.addObject("fatalError",
                    messageSource.getMessage("auth.message.expired", null, locale));
            return modelAndView;
        }

        if(passwordVerificationToken.isExecuted())
        {
            modelAndView.addObject("fatalError",
                    messageSource.getMessage("transfer.used", null, locale));
            return modelAndView;
        }
        if(passwordVerificationToken.getUser() == null)
        {

            modelAndView.addObject("fatalError",
                    messageSource.getMessage("error.unknown", null, locale));
            return modelAndView;
        }
        else
        {
            user = passwordVerificationToken.getUser();
            passwordVerificationTokenService.setVerificationTokenExecuted(passwordVerificationToken);
            modelAndView.addObject("user", user);
            modelAndView.addObject("passFragment" , true);
        }
        return modelAndView;
    }

    @PostMapping("/passwordReset/form/{user}")
    public ModelAndView changePasswordForm(@PathVariable("user") User user, @RequestParam("newPassword") String newPassword,
           @RequestParam("confirmNewPassword") String confirmNewPassword, WebRequest webRequest, RedirectAttributes redirectAttributes)
    {
        ModelAndView modelAndView = new ModelAndView();
        Locale locale = webRequest.getLocale();
        List<String> errors = new ArrayList<>();
        modelAndView.setViewName("main");

        validChangePasswordForm(newPassword, confirmNewPassword, locale, modelAndView, errors, user);

        if(errors.isEmpty())
        {
            redirectAttributes.addFlashAttribute("message",
                    messageSource.getMessage("transfer.defined.success", null, locale));
            modelAndView.setViewName("redirect:/login?lang=" + locale);
            return modelAndView;
        }

        modelAndView.addObject("user", user);
        modelAndView.addObject("date", actualDate.getTime());
        modelAndView.addObject("template", "passwordReset");
        modelAndView.addObject("pageTitle", messageSource.getMessage("password.reset", null, locale));
        modelAndView.addObject("passFragment" , true);
        return modelAndView;
    }

    public ModelAndView validChangePasswordForm(String newPassword, String confirmNewPassword,
           Locale locale, ModelAndView modelAndView, List<String> errors, User user)
    {
        if(user == null)
        {
            modelAndView.addObject("fatalError", messageSource.getMessage("badUser.page", null, locale));
            return modelAndView;
        }

        if(newPassword == null)
        {
            modelAndView.addObject("fatalError", messageSource.getMessage("password.size", null, locale));
            return modelAndView;
        }

        if(!newPassword.equals(confirmNewPassword))
        {
            errors.add(messageSource.getMessage("regForm.error.similar.passwords", null, locale));
        }

        if(newPassword.length() > 40 || newPassword.length() < 8)
        {
            errors.add(messageSource.getMessage("password.size", null, locale));
        }

        if(passwordValidator.passwordIllegalCharacters(newPassword))
        {
            errors.add(messageSource.getMessage("illegal.characters", null, locale));
        }

        if (!errors.isEmpty())
        {
            modelAndView.addObject("fatalErrors", errors);
        }
        else
        {
            String encodedPassword = securityConfig.passwordEncoder().encode(newPassword);
            user.setPassword(encodedPassword);
            userService.updateUser(user);
        }

        return modelAndView;
    }
}
