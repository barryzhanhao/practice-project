package pers.james.practice.axon.jpa.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import pers.james.practice.axon.jpa.aggregate.AccountId;

@Data
@AllArgsConstructor
public class MoneyWithdrawnEvent {
    private AccountId accountId;
    private long amount;
}
