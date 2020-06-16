package bank.controller;

import bank.model.*;
import bank.service.BankFundsService;
import bank.service.DeviceMetadataService;
import bank.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/users")
public class EmployeeController
{
    @Autowired
    private UserService userService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private BankFundsService bankFundsService;

    @Autowired
    private TransfersHistoryController transfersHistoryController;

    @Autowired
    private DeviceMetadataService deviceMetadataService;


    @GetMapping
    public ModelAndView getUsersList(WebRequest webRequest)
    {
        ModelAndView modelAndView = new ModelAndView();
        List<User> users = userService.findAll();
        Long numberOfUsers = userService.countUsers();
        User user = userService.getUserWithAuthentication();
        BigDecimal bankFunds = bankFundsService.getBankFunds().getBankFunds();
        boolean admin = false;

        for(Role r : user.getRoles())
        {
            if(r.getRole().equals("ADMIN"))
            {
                admin = true;
                modelAndView.addObject("bankFunds", bankFunds);
            }
        }

        Iterator<User> i = users.iterator();
        while(i.hasNext())
        {
            User u  = i.next();
            u.setAccountNumber(userService.accountNumberForPage(u.getAccountNumber()));
            if(!admin)
            {
                for(Role r : u.getRoles())
                {
                    if(r.getRole().equals("ADMIN"))
                    {
                        i.remove();
                    }
                }
            }
        }

        modelAndView.addObject("allowRolesButton", false);
        modelAndView.addObject("adminButtons", false);
        modelAndView.addObject("numberOfUsers", numberOfUsers);
        modelAndView.addObject("users", users);
        modelAndView.addObject("user", null);
        modelAndView.addObject("pageTitle",
                messageSource.getMessage("employee.page", null, webRequest.getLocale()));
        modelAndView.setViewName("users");
        return modelAndView;
    }

    @GetMapping("/userId")
    public ModelAndView getUserDetails(@RequestParam("userId") Long userId, WebRequest webRequest,
                                       RedirectAttributes redirectAttributes)
    {
        ModelAndView modelAndView = new ModelAndView();
        Locale locale = webRequest.getLocale();
        if(userId == null)
        {
            modelAndView.setViewName("redirect:/users");
            return modelAndView;
        }
        if(userId < 0)
        {
            modelAndView.setViewName("redirect:/users");
            return modelAndView;
        }

        BigDecimal bankFunds = bankFundsService.getBankFunds().getBankFunds();
        User user = userService.findByUserId(userId);
        User me = userService.getUserWithAuthentication();
        Long numberOfUsers = userService.countUsers();
        List<User> users = null;
        UserDTO userDTO = null;

        if(!me.isEnabled())
        {
            redirectAttributes.addFlashAttribute("fatalError",
                    messageSource.getMessage("auth.message.blocked", null, locale));
            modelAndView.setViewName("redirect:/login");
            return modelAndView;
        }

        if(user.getId() == null)
        {
            users = userService.findAll();

            for(User u : users)
            {
                u.setAccountNumber(userService.accountNumberForPage(u.getAccountNumber()));
            }

            modelAndView.addObject("fatalError",
                    messageSource.getMessage("no.user.found", null, locale));
        }
        else
        {
            userDTO = userService.userToUserDTO(user);
            List<DeviceMetadata> deviceMetadata = deviceMetadataService.findByUserId(userDTO.getId());
            Set<Role> roles = userService.getSetOfRoles("ADMIN");
            String UNKNOWN = messageSource.getMessage("unknown", null, locale);

            for(DeviceMetadata dm : deviceMetadata)
            {
                if(dm.getDeviceDetails() == null || dm.getDeviceDetails().equals(""))
                    dm.setDeviceDetails(UNKNOWN);
                if(dm.getLocation() == null || dm.getLocation().equals(""))
                    dm.setLocation(UNKNOWN);
                if(dm.getLastLogged() == null || dm.getLastLogged().equals(""))
                    dm.setLastLogged(UNKNOWN);
            }

            if(roles.equals(me.getRoles()))
            {
                modelAndView.addObject("disabled", false);
                modelAndView.addObject("admin", roles);
            }
            else
            {
                modelAndView.addObject("disabled", true);
            }

            modelAndView.addObject("transfers", transfersHistoryController.getTransferHistory(userDTO.getAccountNumber(),
                    modelAndView, locale));
            modelAndView.addObject("deviceMetadata", deviceMetadata);
            userDTO.setAccountNumber(userService.accountNumberForPage(user.getAccountNumber()));
        }

        modelAndView.addObject("bankFunds", bankFunds);
        modelAndView.addObject("passwordDisabled", true);
        modelAndView.addObject("numberOfUsers", numberOfUsers);
        modelAndView.addObject("pageTitle",
                messageSource.getMessage("employee.page", null, locale));
        modelAndView.addObject("users", users);
        modelAndView.addObject("user", userDTO);
        modelAndView.addObject("emailBody", new EmailBody());
        modelAndView.setViewName("users");
        return modelAndView;
    }

    @PostMapping("/userId")
    public ModelAndView setUserEnabled(@RequestParam("userId") Long userId, @RequestParam("enable") boolean enable,
                                       WebRequest webRequest, RedirectAttributes redirectAttributes)
    {
        Locale locale = webRequest.getLocale();

        if(!userService.getUserWithAuthentication().isEnabled())
        {
            redirectAttributes.addFlashAttribute("fatalError",
                    messageSource.getMessage("auth.message.blocked", null, locale));
            return new ModelAndView("redirect:/login");
        }

        if(enable)
        {
            userService.setUserEnabled(true, userId);
            redirectAttributes.addFlashAttribute("message",
                    messageSource.getMessage("employee.user.enable.now", null, locale));
        }
        else
        {
            userService.setUserEnabled(false, userId);
            redirectAttributes.addFlashAttribute("message",
                    messageSource.getMessage("employee.user.disable.now", null, locale));
        }

        return new ModelAndView("redirect:/users/userId?userId=" + userId);
    }

}
