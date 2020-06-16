package bank.repository;

import bank.model.PasswordVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordVerificationRepository extends JpaRepository<PasswordVerificationToken, Long>
{
    PasswordVerificationToken findByToken(String token);
}
