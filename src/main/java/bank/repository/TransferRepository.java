package bank.repository;

import bank.model.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long>
{
    @Modifying
    @Transactional
    @Query("UPDATE Transfer t SET t.executed=:executed WHERE t.id=:id")
    void setTransferExecuted(@Param("executed") boolean executed, @Param("id") Long id);

    List<Transfer> findBySenderRecipientContainsAndExecutedTrueOrderByIdDesc(String accountNumber);
}
