package bank.service;

import bank.model.BankFunds;
import bank.repository.BankFundsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class BankFundsService
{
    @Autowired
    private BankFundsRepository bankFundsRepository;

    public BankFunds getBankFunds()
    {
        return bankFundsRepository.findFirstByOrderByIdAsc();
    }

    public void updateBankFunds(BankFunds addTaxWithBankFunds)
    {
        bankFundsRepository.save(addTaxWithBankFunds);
    }
}
