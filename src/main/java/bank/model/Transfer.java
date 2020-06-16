package bank.model;


import bank.Validator.ValidOnlyNumbers;
import bank.Validator.ValidOnlyNumbersAndString;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transfer")
public class Transfer
{
    @Id
    @Column(name = "transfer_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;

    @Transient
    @Size(min = 24, max = 29, message = "{accountNumber.size}")
    @ValidOnlyNumbers(message = "{illegal.characters}")
    private String recipient;

    private String senderRecipient;

    @DecimalMin(value = "6.00", message = "{transfer.small.amount}")
    @DecimalMax(value = "100000.00", message = "{transfer.too.large.amount}")
    @NotNull(message = "{transfer.small.amount}")
    private BigDecimal amount;

    @DecimalMin(value = "0.00")
    private BigDecimal senderBalance;

    @DecimalMin(value = "0.00")
    private BigDecimal recipientBalance;

    private String date;

    @ValidOnlyNumbersAndString(message = "{illegal.characters}")
    @NotEmpty(message = "{transfer.error.subject}")
    private String subject;

    @ValidOnlyNumbersAndString(message = "{illegal.characters}")
    @NotEmpty(message = "{transfer.error.subject}")
    private String recipientName;

    @NotNull
    private boolean executed;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSenderRecipient() {
        return senderRecipient;
    }

    public void setSenderRecipient(String senderRecipient)
    {
        this.senderRecipient = senderRecipient;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public BigDecimal getSenderBalance() {
        return senderBalance;
    }

    public void setSenderBalance(BigDecimal senderBalance) {
        this.senderBalance = senderBalance;
    }

    public BigDecimal getRecipientBalance() {
        return recipientBalance;
    }

    public void setRecipientBalance(BigDecimal recipientBalance) {
        this.recipientBalance = recipientBalance;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isExecuted() {
        return executed;
    }

    public void setExecuted(boolean executed) {
        this.executed = executed;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }
}
