package bank.repository;

import bank.model.BankFunds;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface BankFundsRepository extends JpaRepository<BankFunds, Long>
{
    BankFunds findFirstByOrderByIdAsc();
}
