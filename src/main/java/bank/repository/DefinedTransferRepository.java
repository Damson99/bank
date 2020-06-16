package bank.repository;

import bank.model.DefinedTransfer;
import bank.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DefinedTransferRepository extends JpaRepository<DefinedTransfer, Long>
{
    List<DefinedTransfer> findByUserId(Long userId);

    User findByRecipient(String recipient);
}
