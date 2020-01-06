package pers.james.practice.axon;


import java.util.concurrent.TimeUnit;
import org.axonframework.config.Configuration;
import org.axonframework.config.DefaultConfigurer;
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;
import pers.james.practice.axon.aggregate.AccountId;
import pers.james.practice.axon.aggregate.BankAccount;
import pers.james.practice.axon.command.CreateAccountCommand;
import pers.james.practice.axon.command.WithdrawMoneyCommand;

public class Application {

    public static void main(String[] args) throws InterruptedException {
        Configuration config = DefaultConfigurer.defaultConfiguration().configureAggregate(BankAccount.class)
            .configureEmbeddedEventStore(configuration -> new InMemoryEventStorageEngine()).buildConfiguration();
        config.start();
        AccountId accountId=new AccountId();
        config.commandGateway().send(new CreateAccountCommand(accountId,"haha",1000));
        config.commandGateway().send(new WithdrawMoneyCommand(accountId,500));
        config.commandGateway().send(new WithdrawMoneyCommand(accountId,200));
    }

}
