package pers.james.practice.r2dbc.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pers.james.practice.r2dbc.internal.entity.Person;
import pers.james.practice.r2dbc.repo.PersonRepository;
import reactor.test.StepVerifier;

import java.util.Arrays;

@Slf4j
@RestController
public class SampleController {

    @Autowired
    private PersonRepository repository;

    @Autowired
    private DatabaseClient database;

    @GetMapping("/hello")
    public String get() {

        var statements = Arrays.asList(//
                "DROP TABLE IF EXISTS PERSON;",
                "CREATE TABLE PERSON (id VARCHAR(255) PRIMARY KEY,name VARCHAR(255),age INT)");

        statements.forEach(it -> database.sql(it)
                .fetch()
                .rowsUpdated()
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete());


        repository.save(new Person("joe", "Joe", 34))
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();

        repository.findAll()
                .doOnNext(it -> log.info(it.toString()))
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();

        return "2222";
    }

}
