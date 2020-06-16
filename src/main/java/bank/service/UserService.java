package bank.service;

import bank.config.SecurityConfig;
import bank.model.Role;
import bank.model.User;
import bank.model.UserDTO;
import bank.repository.RoleRepository;
import bank.repository.UserRepository;
import bank.utils.ActualDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

@Service
public class UserService
{
    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ActualDate actualDate;


    public User findByAccountNumber(String accountNumber)
    {
        return userRepository.findByAccountNumber(accountNumber);
    }

    public User findByUserEmail(String email)
    {
        return userRepository.findByEmail(email);
    }

    public User findByUserPersonalIdNumber(String personalIdNumber)
    {
        return userRepository.findByPersonalIdNumber(personalIdNumber);
    }

    public User findByUserPhoneNumber(String phoneNumber)
    {
        return userRepository.findByPhoneNumber(phoneNumber);
    }

    public User findByUserId(Long id)
    {
        Optional<User> userOptional = userRepository.findById(id);
        User user = new User();

        if(userOptional.isPresent())
        {
            user = userOptional.get();
        }

        return user;
    }

    public void setUserEnabled(boolean enabled, Long id)
    {
        userRepository.setUserEnabled(enabled, id);
    }

    public List<User> findAll()
    {
        return userRepository.findAll();
    }

    public Long countUsers()
    {
        return userRepository.count();
    }

    public User getUserWithAuthentication()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return  userRepository.findByPersonalIdNumber(authentication.getName());
    }

    public void saveUser(User user)
    {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String ip = request.getRemoteAddr();
        Role userRoles = roleRepository.findByRole("USER");
        StringBuilder accountNumber = new StringBuilder();
        for (int i = 0; i < 6; i++)
        {
            int random = (int) (Math.random() * (9999 - 1000)) + 1000;
            String toString = Integer.toString(random);
            accountNumber.append(toString);
        }

        user.setAccountNumber(accountNumber.toString());
        user.setPassword(securityConfig.passwordEncoder().encode(user.getPassword()));
        user.setEnabled(true);
        user.setCreatedAccountTime(actualDate.getTime());
        user.setIp(ip);
        user.setFunds(new BigDecimal("10000.00"));
        user.setRoles(new HashSet<>(Arrays.asList(userRoles)));
        userRepository.save(user);
    }

    public void updateUser(User userDTO)
    {
        userRepository.save(userDTO);
    }

    public void updateUserFunds(BigDecimal funds, String personalIdNumber)
    {
        User user = new User();
        user.setFunds(funds);
        user.setPersonalIdNumber(personalIdNumber);
        userRepository.updateUserFunds(funds, personalIdNumber);
    }

    public Set<Role> getSetOfRoles(String role)
    {
        Set<Role> roles = new HashSet<>();
        Role userRole = new Role();
        Role employeeRole = new Role();
        Role adminRole = new Role();

        adminRole.setRole("ADMIN");
        employeeRole.setRole("EMPLOYEE");
        userRole.setRole("USER");

        adminRole.setId(2);
        employeeRole.setId(1);
        userRole.setId(0);

        switch (role)
        {
            case "ADMIN":
                roles.add(adminRole);
                roles.add(employeeRole);
                roles.add(userRole);
                break;

            case "EMPLOYEE":
                roles.add(employeeRole);
                roles.add(userRole);
                break;

            default:
                roles.add(userRole);
                break;
        }
        return roles;
    }

    public void deleteUser(User user)
    {
        userRepository.delete(user);
    }

    public User userDTOToUser(UserDTO userDTO)
    {
        User user = new User();
        user.setId(userDTO.getId());
        user.setAccountNumber(userDTO.getAccountNumber());
        user.setEmail(userDTO.getEmail());
        user.setName(userDTO.getName());
        user.setLastName(userDTO.getLastName());
        user.setAge(userDTO.getAge());
        user.setPersonalIdNumber(userDTO.getPersonalIdNumber());
        user.setStreet(userDTO.getStreet());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setZipCode(userDTO.getZipCode());
        user.setCountry(userDTO.getCountry());
        user.setEnabled(userDTO.isEnabled());
        user.setCreatedAccountTime(userDTO.getCreatedAccountTime());
        user.setIp(userDTO.getIp());
        user.setRoles(userDTO.getRoles());
        return user;
    }

    public UserDTO userToUserDTO(User user)
    {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setAccountNumber(user.getAccountNumber());
        userDTO.setEmail(user.getEmail());
        userDTO.setName(user.getName());
        userDTO.setLastName(user.getLastName());
        userDTO.setAge(user.getAge());
        userDTO.setPersonalIdNumber(user.getPersonalIdNumber());
        userDTO.setStreet(user.getStreet());
        userDTO.setPhoneNumber(user.getPhoneNumber());
        userDTO.setZipCode(user.getZipCode());
        userDTO.setCountry(user.getCountry());
        userDTO.setEnabled(user.isEnabled());
        userDTO.setCreatedAccountTime(user.getCreatedAccountTime());
        userDTO.setIp(user.getIp());
        userDTO.setRoles(user.getRoles());
        return userDTO;
    }

    public String accountNumberForPage(String accountNumber)
    {
        accountNumberForDB(accountNumber);
        StringBuilder builder = new StringBuilder();

        if(accountNumber.length() == 24)
        {
            char[] ch = accountNumber.toCharArray();
            for(int i = 0; i <= 23; i++)
            {
                builder.append(ch[i]);
                if(i == 3 || i == 7 || i == 11 || i == 15 || i == 19)
                {
                    builder.append(" ");
                }
            }
        }
        else
        {
            builder.append(accountNumber);
        }
        return builder.toString();
    }

    public String accountNumberForDB(String accountNumber)
    {
        return accountNumber.replaceAll("\\s+", "");
    }
}
