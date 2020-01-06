package pers.james.practice.axon.jpa.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;
import pers.james.practice.axon.jpa.aggregate.AccountId;

@Data
@AllArgsConstructor
public class WithdrawMoneyCommand {
    @TargetAggregateIdentifier
    private AccountId accountId;
    private long amount;
}
