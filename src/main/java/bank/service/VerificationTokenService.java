package bank.service;

import bank.model.Transfer;
import bank.model.VerificationToken;
import bank.repository.VerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class VerificationTokenService
{
    @Autowired
    private VerificationRepository verificationRepository;


    public VerificationToken getVerificationToken(String token)
    {
        return verificationRepository.findByToken(token);
    }

    public void createVerificationToken(Transfer transfer, String token)
    {
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setTransfer(transfer);
        verificationToken.setToken(token);
        verificationToken.setExpiryDate(new Date());
        verificationRepository.save(verificationToken);
    }
}
