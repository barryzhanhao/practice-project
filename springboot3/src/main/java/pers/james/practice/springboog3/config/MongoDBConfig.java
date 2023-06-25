package pers.james.practice.springboog3.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoDBConfig {
    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create("mongodb://admin:123456@127.0.0.1:27017/admin");

    }
    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), "admin");
    }
}