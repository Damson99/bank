package bank.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bank_funds")
public class BankFunds
{
    @Id
    private int id;

    @DecimalMin(value = "0.00")
    @DecimalMax(value = "99999999.99")
    private BigDecimal bankFunds;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public BigDecimal getBankFunds() {
        return bankFunds;
    }

    public void setBankFunds(BigDecimal bankFunds) {
        this.bankFunds = bankFunds;
    }
}
