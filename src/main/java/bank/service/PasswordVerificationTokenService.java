package bank.service;

import bank.model.PasswordVerificationToken;
import bank.model.User;
import bank.repository.PasswordVerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

@Service
public class PasswordVerificationTokenService
{
    @Autowired
    private PasswordVerificationRepository passwordVerificationRepository;

    private static final int MINUTE_IN_MILLIS = 60000;


    public PasswordVerificationToken getVerificationToken(String token)
    {
        return passwordVerificationRepository.findByToken(token);
    }

    public void createVerificationToken(User user, String token)
    {
        PasswordVerificationToken passwordVerificationToken = new PasswordVerificationToken();
        passwordVerificationToken.setUser(user);
        passwordVerificationToken.setToken(token);
        passwordVerificationToken.setExpiryDate(new Date());
        passwordVerificationToken.setExecuted(false);
        passwordVerificationRepository.save(passwordVerificationToken);
    }

    public void setVerificationTokenExecuted(PasswordVerificationToken passwordVerificationToken)
    {
        passwordVerificationToken.setExecuted(true);
        passwordVerificationRepository.save(passwordVerificationToken);
    }

    public boolean expiryDate(Date date)
    {
        long now = Calendar.getInstance().getTimeInMillis();
        long expiryDate = date.getTime() + MINUTE_IN_MILLIS * 5;
        if((now - expiryDate) < 0)
        {
            return false;
        }
        else
        {
            return true;
        }
    }
}
