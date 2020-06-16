package bank.model;


import org.springframework.context.ApplicationEvent;

import java.util.Locale;

public class SenderEvent extends ApplicationEvent
{
    private Locale locale;
    private EmailBody emailBody;

    public SenderEvent(Locale locale, EmailBody emailBody)
    {
        super(emailBody);
        this.locale = locale;
        this.emailBody = emailBody;
    }

    public EmailBody getEmailBody()
    {
        return emailBody;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public EmailBody getAppUrl() {
        return emailBody;
    }

    public void setAppUrl(EmailBody appUrl) {
        this.emailBody = appUrl;
    }
}
