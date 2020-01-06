package pers.james.practice.axon.jpa.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import pers.james.practice.axon.jpa.aggregate.AccountId;

@Data
@AllArgsConstructor
public class CreateAccountCommand {
    private AccountId accountId;
    private String accountName;
    private long amount;
}
