package pers.james.practice.axon.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import pers.james.practice.axon.aggregate.AccountId;

@Data
@AllArgsConstructor
public class CreateAccountCommand {
    private AccountId accountId;
    private String accountName;
    private long amount;
}
