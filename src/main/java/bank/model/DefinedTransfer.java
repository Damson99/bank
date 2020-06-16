package bank.model;

import bank.Validator.ValidOnlyNumbers;
import bank.Validator.ValidOnlyNumbersAndString;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "defined_transfer")
public class DefinedTransfer
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Size(min = 24, max = 29, message = "{accountNumber.size}")
    @ValidOnlyNumbers(message = "{illegal.characters}")
    private String recipient;

    @ValidOnlyNumbersAndString(message = "{illegal.characters}")
    @NotEmpty(message = "{transfer.error.subject}")
    private String subject;

    @ValidOnlyNumbersAndString(message = "{illegal.characters}")
    @NotEmpty(message = "{transfer.error.subject}")
    private String recipientName;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }
}
