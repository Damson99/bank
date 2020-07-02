package bank.model;


import bank.Validator.ValidOnlyNumbers;
import bank.Validator.ValidOnlyNumbersAndString;
import bank.Validator.ValidOnlyString;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Set;

@Data
@Entity
@Table(name = "user")
public class User
{
    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String accountNumber;

    @Email
    @Size(min = 10, max = 40, message = "{email.size}")
    private String email;

    private String password;

    @Transient
    private String confirmPassword;

    @Size(min = 3, max = 32, message = "{name.size}")
    @ValidOnlyString(message = "{illegal.characters}")
    private String name;

    @Size(min = 3, max = 32, message = "{lastName.size}")
    @ValidOnlyString(message = "{illegal.characters}")
    private String lastName;

    @Range(min = 18, max = 200, message = "{age.range}")
    private int age;

    @Size(min = 11, max = 11, message = "{personalIdNumber.size}")
    @ValidOnlyNumbers(message = "{illegal.characters}")
    private String personalIdNumber;

    @Size(min = 8, max = 50, message = "{street.size}")
    @ValidOnlyNumbersAndString(message = "{illegal.characters}")
    private String street;

    @Size(min = 9, max = 9, message = "{phoneNumber.size}")
    @ValidOnlyNumbers(message = "{illegal.characters}")
    private String phoneNumber;

    @Size(min = 10, max = 50, message = "{zipCode.size}")
    @ValidOnlyNumbersAndString(message = "{illegal.characters}")
    private String zipCode;

    @NotEmpty(message = "{country.notEmpty}")
    @ValidOnlyString(message = "{illegal.characters}")
    private String country;

    private boolean enabled;

    private String createdAccountTime;

    private String ip;

    @DecimalMin(value = "0.00")
    @DecimalMax(value = "99999999.99")
    private BigDecimal funds;

    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;


    private User(Long id, String accountNumber, @Email @Size(min = 10, max = 40, message = "{email.size}") String email, String password, String confirmPassword, @Size(min = 3, max = 32, message = "{name.size}") String name, @Size(min = 3, max = 32, message = "{lastName.size}") String lastName, @Range(min = 18, max = 200, message = "{age.range}") int age, @Size(min = 11, max = 11, message = "{personalIdNumber.size}") String personalIdNumber, @Size(min = 8, max = 50, message = "{street.size}") String street, @Size(min = 9, max = 9, message = "{phoneNumber.size}") String phoneNumber, @Size(min = 10, max = 50, message = "{zipCode.size}") String zipCode, @NotEmpty(message = "{country.notEmpty}") String country, boolean enabled, String createdAccountTime, String ip, @DecimalMin(value = "0.00") @DecimalMax(value = "99999999.99") BigDecimal funds, Set<Role> roles)
    {
        this.id = id;
        this.accountNumber = accountNumber;
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.name = name;
        this.lastName = lastName;
        this.age = age;
        this.personalIdNumber = personalIdNumber;
        this.street = street;
        this.phoneNumber = phoneNumber;
        this.zipCode = zipCode;
        this.country = country;
        this.enabled = enabled;
        this.createdAccountTime = createdAccountTime;
        this.ip = ip;
        this.funds = funds;
        this.roles = roles;
    }

    public static class UserBuilder
    {
        private Long id;
        private String accountNumber;
        private String email;
        private String password;
        private String confirmPassword;
        private String name;
        private String lastName;
        private int age;
        private String personalIdNumber;
        private String street;
        private String phoneNumber;
        private String zipCode;
        private String country;
        private boolean enabled;
        private String createdAccountTime;
        private String ip;
        private BigDecimal funds;
        private Set<Role> roles;


        public UserBuilder setId(Long id) {
            this.id = id;
            return this;
        }

        public UserBuilder setAccountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
            return this;
        }

        public UserBuilder setEmail(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder setPassword(String password) {
            this.password = password;
            return this;
        }

        public UserBuilder setConfirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
            return this;
        }

        public UserBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public UserBuilder setLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public UserBuilder setAge(int age) {
            this.age = age;
            return this;
        }

        public UserBuilder setPersonalIdNumber(String personalIdNumber) {
            this.personalIdNumber = personalIdNumber;
            return this;
        }

        public UserBuilder setStreet(String street) {
            this.street = street;
            return this;
        }

        public UserBuilder setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public UserBuilder setZipCode(String zipCode) {
            this.zipCode = zipCode;
            return this;
        }

        public UserBuilder setCountry(String country) {
            this.country = country;
            return this;
        }

        public UserBuilder setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public UserBuilder setCreatedAccountTime(String createdAccountTime) {
            this.createdAccountTime = createdAccountTime;
            return this;
        }

        public UserBuilder setIp(String ip) {
            this.ip = ip;
            return this;
        }

        public UserBuilder setFunds(BigDecimal funds) {
            this.funds = funds;
            return this;
        }

        public UserBuilder setRoles(Set<Role> roles) {
            this.roles = roles;
            return this;
        }


        public User build()
        {
            return new User(id, accountNumber, email, password, confirmPassword, name, lastName, age, personalIdNumber, street, phoneNumber, zipCode, country, enabled, createdAccountTime, ip, funds, roles);
        }
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getPersonalIdNumber() {
        return personalIdNumber;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setPersonalIdNumber(String personalIdNumber) {
        this.personalIdNumber = personalIdNumber;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getCreatedAccountTime() {
        return createdAccountTime;
    }

    public void setCreatedAccountTime(String createdAccountTime) {
        this.createdAccountTime = createdAccountTime;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public BigDecimal getFunds() {
        return funds;
    }

    public void setFunds(BigDecimal funds) {
        this.funds = funds;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
}
