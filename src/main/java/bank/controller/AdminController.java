package bank.controller;

import bank.model.GeoLocation;
import bank.model.Role;
import bank.model.User;
import bank.model.UserDTO;
import bank.service.BankFundsService;
import bank.service.GeoLocationService;
import bank.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/console")
public class AdminController
{
    @Autowired
    private GeoLocationService geoLocalizationService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private UserService userService;

    @Autowired
    private LoginController loginController;

    @Autowired
    private BankFundsService bankFundsService;


    @GetMapping
    public ModelAndView main(WebRequest webRequest)
    {
        ModelAndView modelAndView = new ModelAndView();

        modelAndView.addObject("pageTitle",
                messageSource.getMessage("console.page", null, webRequest.getLocale()));
        modelAndView.setViewName("location");
        return modelAndView;
    }

    @PostMapping
    public GeoLocation getLocation(@RequestParam(value = "ipAddress", required = true) String ipAddress) throws Exception
    {
        return geoLocalizationService.getLocation(ipAddress);
    }

    @PostMapping("/update")
    public ModelAndView userUpdate(@Valid UserDTO userDTO, BindingResult bindingResult, WebRequest webRequest,
                                   RedirectAttributes redirectAttributes)
    {
        ModelAndView modelAndView = new ModelAndView();
        Locale locale = webRequest.getLocale();


        if(!userService.getUserWithAuthentication().isEnabled())
        {
            redirectAttributes.addFlashAttribute("fatalError",
                    messageSource.getMessage("auth.message.blocked", null, locale));
            return new ModelAndView("redirect:/login");
        }

        loginController.checkIsExists(userDTO.getId(), userDTO.getEmail(), userDTO.getPersonalIdNumber(), userDTO.getPhoneNumber(),
                locale, bindingResult);

        if(bindingResult.hasErrors())
        {
            List<String> objectErrors = new ArrayList<>();
            for(Object object : bindingResult.getAllErrors())
            {
                if(object instanceof ObjectError)
                {
                    objectErrors.add(((ObjectError) object).getDefaultMessage());
                }
            }

            redirectAttributes.addFlashAttribute("fatalErrors", objectErrors);
            modelAndView.setViewName("redirect:/users/userId?userId=" + userDTO.getId());
            return modelAndView;
        }

        User userBefore = userService.findByUserId(userDTO.getId());
        Set<Role> roles = userDTO.getRoles();

        Role userRole = new Role();
        userRole.setId(0);
        userRole.setRole("USER");
        Role employeeRole = new Role();
        employeeRole.setId(1);
        employeeRole.setRole("EMPLOYEE");
        Role adminRole = new Role();
        adminRole.setId(2);
        adminRole.setRole("ADMIN");

        for(Role r : roles)
        {
            if(r.getRole().equals("EMPLOYEE"))
            {
                roles.add(userRole);
            }
            if(r.getRole().equals("ADMIN"))
            {
                roles.add(userRole);
                roles.add(employeeRole);
            }
        }

        userDTO.setAccountNumber(userService.accountNumberForDB(userDTO.getAccountNumber()));
        userDTO.setRoles(roles);
        User user = userService.userDTOToUser(userDTO);
        user.setFunds(userBefore.getFunds());
        user.setPassword(userBefore.getPassword());
        userDTO.setAccountNumber(userService.accountNumberForPage(userDTO.getAccountNumber()));
        userService.updateUser(user);

        redirectAttributes.addFlashAttribute("message",
                messageSource.getMessage("user.updated", null, locale));
        modelAndView.setViewName("redirect:/users/userId?userId=" + user.getId());
        return modelAndView;
    }

    @PostMapping("/delete")
    public ModelAndView deleteUser(@RequestParam("userId") Long userId, WebRequest webRequest, RedirectAttributes redirectAttributes)
    {
        User user = userService.findByUserId(userId);
        User me = userService.getUserWithAuthentication();
        if(user.getId().equals(me.getId()))
        {
            redirectAttributes.addFlashAttribute("fatalError",
                    messageSource.getMessage("admin.delete.me", null, webRequest.getLocale()));
            return new ModelAndView("redirect:/users");
        }

        redirectAttributes.addFlashAttribute("message",
                messageSource.getMessage("admin.user.deleted", null, webRequest.getLocale()));
        userService.deleteUser(user);
        return new ModelAndView("redirect:/users");
    }
}
