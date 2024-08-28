package pers.james.practice.r2dbc.repo;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import pers.james.practice.r2dbc.internal.entity.Person;

public interface PersonRepository extends ReactiveCrudRepository<Person, Long> {


}
