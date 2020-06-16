package bank.utils;

import bank.model.EmailBody;
import bank.model.SenderEvent;
import bank.service.VerificationTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

@Component
public class Sender implements ApplicationListener<SenderEvent>
{
    @Autowired
    private VerificationTokenService verificationTokenService;

    @Autowired
    private MessageSource messageSource;

    @Value("${email.support.email}")
    private String SENDER_GMAIL;

    @Value("${email.support.password}")
    private String SENDER_PASSWORD;


    @Override
    public void onApplicationEvent(SenderEvent event)
    {
        try
        {
            this.sender(event);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    private void sender(SenderEvent event) throws Throwable
    {
        EmailBody emailBody = event.getEmailBody();
        String recipient = emailBody.getRecipient();

        Properties props = System.getProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new javax.mail.Authenticator()
        {
            protected PasswordAuthentication getPasswordAuthentication()
            {
                return new PasswordAuthentication(SENDER_GMAIL, SENDER_PASSWORD);
            }
        });

        Message mailMessage = new MimeMessage(session);

        mailMessage.setFrom(new InternetAddress(SENDER_GMAIL));
        mailMessage.addRecipient(Message.RecipientType.BCC, new InternetAddress(recipient));
        mailMessage.setSubject(emailBody.getSubject());
        mailMessage.setContent(emailBody.getBody(), "text/html; charset=utf-8;");
        mailMessage.setSentDate(new Date());

        if(emailBody.getFile() != null)
        {
            MimeBodyPart message = new MimeBodyPart();
            MimeMultipart mimeMultipart = new MimeMultipart();
            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            message.setContent(emailBody.getBody(), "text/html");
            mimeMultipart.addBodyPart(message);

            mimeBodyPart.setHeader("Content-ID", emailBody.getFile().toString());
            mimeBodyPart.setDisposition(MimeBodyPart.INLINE);

            try
            {
                mimeBodyPart.attachFile(emailBody.getFile());
            }
            catch (IOException ex)
            {
            }

            mimeMultipart.addBodyPart(mimeBodyPart);
            mailMessage.setContent(mimeMultipart);
        }

        Transport transport = session.getTransport("smtp");
        try
        {
            transport.connect(SENDER_GMAIL, SENDER_PASSWORD);
            transport.sendMessage(mailMessage, mailMessage.getAllRecipients());
        }
        finally
        {
            transport.close();
        }
    }
}
