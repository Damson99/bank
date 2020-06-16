package bank.model;

import bank.Validator.ValidOnlyNumbers;
import bank.Validator.ValidOnlyNumbersAndString;
import bank.Validator.ValidOnlyString;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.*;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO
{
    private Long id;

    @Size(min = 24, max = 29, message = "{accountNumber.size}")
    @ValidOnlyNumbers(message = "{illegal.characters}")
    private String accountNumber;

    @Email
    @Size(min = 10, max = 40, message = "{email.size}")
    private String email;

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

    private Set<Role> roles;


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

    public void setPersonalIdNumber(String personalIdNumber) {
        this.personalIdNumber = personalIdNumber;
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

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
}
