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

    private Transfer(Long id, @Size(min = 24, max = 29, message = "{accountNumber.size}") String recipient, String senderRecipient, @DecimalMin(value = "6.00", message = "{transfer.small.amount}") @DecimalMax(value = "100000.00", message = "{transfer.too.large.amount}") @NotNull(message = "{transfer.small.amount}") BigDecimal amount, @DecimalMin(value = "0.00") BigDecimal senderBalance, @DecimalMin(value = "0.00") BigDecimal recipientBalance, String date, @NotEmpty(message = "{transfer.error.subject}") String subject, @NotEmpty(message = "{transfer.error.subject}") String recipientName, @NotNull boolean executed) {
        this.id = id;
        this.recipient = recipient;
        this.senderRecipient = senderRecipient;
        this.amount = amount;
        this.senderBalance = senderBalance;
        this.recipientBalance = recipientBalance;
        this.date = date;
        this.subject = subject;
        this.recipientName = recipientName;
        this.executed = executed;
    }


    public static class TransferBuilder
    {
        private Long id;
        private String recipient;
        private String senderRecipient;
        private BigDecimal amount;
        private BigDecimal senderBalance;
        private BigDecimal recipientBalance;
        private String date;
        private String subject;
        private String recipientName;
        private boolean executed;


        public TransferBuilder setId(Long id) {
            this.id = id;
            return this;
        }

        public TransferBuilder setRecipient(String recipient) {
            this.recipient = recipient;
            return this;
        }

        public TransferBuilder setSenderRecipient(String senderRecipient) {
            this.senderRecipient = senderRecipient;
            return this;
        }

        public TransferBuilder setAmount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public TransferBuilder setSenderBalance(BigDecimal senderBalance) {
            this.senderBalance = senderBalance;
            return this;
        }

        public TransferBuilder setRecipientBalance(BigDecimal recipientBalance) {
            this.recipientBalance = recipientBalance;
            return this;
        }

        public TransferBuilder setDate(String date) {
            this.date = date;
            return this;
        }

        public TransferBuilder setSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public TransferBuilder setRecipientName(String recipientName) {
            this.recipientName = recipientName;
            return this;
        }

        public TransferBuilder setExecuted(boolean executed) {
            this.executed = executed;
            return this;
        }


        public Transfer build()
        {
            return new Transfer(id, recipient, senderRecipient, amount, senderBalance, recipientBalance, date, subject, recipientName, executed);
        }
    }


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
