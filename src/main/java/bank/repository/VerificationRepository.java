package bank.repository;

import bank.model.VerificationToken;
import bank.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationRepository extends JpaRepository<VerificationToken, Long>
{
    VerificationToken findByToken(String token);
}
