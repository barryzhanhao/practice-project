package pers.james.practice.axon.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import pers.james.practice.axon.aggregate.AccountId;

@Data
@AllArgsConstructor
public class AccountCreatedEvent {
    private AccountId accountId;
    private String accountName;
    private long amount;
}
