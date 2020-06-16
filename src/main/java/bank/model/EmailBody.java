package bank.model;

import bank.Validator.ValidOnlyNumbersAndString;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;
import java.io.File;


public class EmailBody
{
    @Size(max = 60, message = "{zipCode.size}")
    @ValidOnlyNumbersAndString(message = "{illegal.characters}")
    private String subject;

    @Size(max = 2000, message = "{zipCode.size}")
    @ValidOnlyNumbersAndString(message = "{illegal.characters}")
    private String body;

    @Email
    private String recipient;

    private File file;


    public EmailBody(String subject, String body, String recipient)
    {
        this.subject = subject;
        this.body = body;
        this.recipient = recipient;
    }

    public EmailBody()
    {

    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
