package pers.james.practice.axon.jpa.aggregate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import pers.james.practice.axon.jpa.command.CreateAccountCommand;
import pers.james.practice.axon.jpa.command.WithdrawMoneyCommand;
import pers.james.practice.axon.jpa.event.AccountCreatedEvent;
import pers.james.practice.axon.jpa.event.MoneyWithdrawnEvent;

import java.math.BigDecimal;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Data
@Slf4j
@Entity
@Aggregate(repository = "accountRepository")
@NoArgsConstructor
public class BankAccount {

    @AggregateIdentifier
    private AccountId accountId;
    private String accountName;
    private BigDecimal balance;

    @CommandHandler
    public BankAccount(CreateAccountCommand command) {
        apply(new AccountCreatedEvent(command.getAccountId(), command.getAccountName(), command.getAmount()));
    }

    @CommandHandler
    public void handle(WithdrawMoneyCommand command) {
        apply(new MoneyWithdrawnEvent(command.getAccountId(), command.getAmount()));
    }

    @EventHandler
    public void on(AccountCreatedEvent event) {
        this.accountId = event.getAccountId();
        this.accountName = event.getAccountName();
        this.balance = new BigDecimal(event.getAmount());
        log.info("Account {} is created with balance {}", accountId, this.balance);
    }

    @EventHandler
    public void on(MoneyWithdrawnEvent event) {
        BigDecimal result = this.balance.subtract(new BigDecimal(event.getAmount()));
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            log.error("Cannot withdraw more money than the balance!");
        } else {
            this.balance = result;
            log.info("Withdraw {} from account {}, balance result: {}", event.getAmount(), accountId, balance);
        }
    }

    @Id
    public String getAccountId() {
        return accountId.toString();
    }

    public void setAccountId(String accountId) {
        this.accountId = new AccountId(accountId);
    }

    @Column
    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    @Column
    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

}
