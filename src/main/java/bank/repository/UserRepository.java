package bank.repository;

import bank.model.Role;
import bank.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;


@Repository
public interface UserRepository extends JpaRepository<User, Long>
{

    User findByEmail(String email);

    User findByPersonalIdNumber(String personalIdNumber);

    User findByAccountNumber(String accountNumber);

    User findByPhoneNumber(String phoneNumber);

    Optional<User> findById(Long id);

    @Modifying
    @Query("UPDATE User u SET u.enabled=:enabled WHERE u.id=:id")
    @Transactional
    void setUserEnabled(@Param("enabled") boolean enabled, @Param("id") Long id);

    @Modifying
    @Query("UPDATE User u SET u.funds=:funds WHERE u.personalIdNumber=:personalIdNumber")
    @Transactional
    void updateUserFunds(@Param("funds") BigDecimal funds, @Param("personalIdNumber") String personalIdNumber);

    @Modifying
    @Query("UPDATE User u SET u.roles=:roles WHERE u.personalIdNumber=:personalIdNumber")
    @Transactional
    void setRoles(@Param("roles") Set<Role> roles, @Param("personalIdNumber") String personalIdNumber);
}
