package pers.james.practice.axon.jpa.controller;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pers.james.practice.axon.jpa.aggregate.AccountId;
import pers.james.practice.axon.jpa.command.CreateAccountCommand;
import pers.james.practice.axon.jpa.command.WithdrawMoneyCommand;

@Slf4j
@RestController
@RequestMapping("/bank")
public class BankAccountController {

    @Autowired
    private CommandGateway commandGateway;

    @GetMapping("/test")
    public String test() {
        AccountId accountId = new AccountId();
        commandGateway.send(new CreateAccountCommand(accountId, "MyAccount", 1000));
        commandGateway.send(new WithdrawMoneyCommand(accountId, 500));
        commandGateway.send(new WithdrawMoneyCommand(accountId, 300));
        commandGateway.send(new CreateAccountCommand(accountId, "MyAccount", 1000));
        commandGateway.send(new WithdrawMoneyCommand(accountId, 500));
        return "OK";
    }
}
