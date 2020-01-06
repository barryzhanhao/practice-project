package pers.james.practice.axon.jpa.config;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.common.jpa.EntityManagerProvider;
import org.axonframework.common.jpa.SimpleEntityManagerProvider;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.config.EventProcessingModule;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.SimpleEventBus;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;
import org.axonframework.messaging.interceptors.TransactionManagingInterceptor;
import org.axonframework.modelling.command.GenericJpaRepository;
import org.axonframework.modelling.command.Repository;
import org.axonframework.spring.config.AnnotationDriven;
import org.axonframework.spring.config.SpringAxonAutoConfigurer;
import org.axonframework.spring.messaging.unitofwork.SpringTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import pers.james.practice.axon.jpa.aggregate.BankAccount;

@Configuration
@AnnotationDriven
@EnableJpaRepositories
@EnableTransactionManagement
@Import(SpringAxonAutoConfigurer.ImportSelector.class)
public class JpaConfig {

//    @Bean
//    public LocalContainerEntityManagerFactoryBean entityManagerFactory( DataSource dataSource) {
//        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
//        vendorAdapter.setGenerateDdl(true);
//        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
//        factory.setJpaVendorAdapter(vendorAdapter);
//        factory.setPackagesToScan("com.acme.domain");
//        factory.setDataSource(dataSource);
//        return factory;
//    }
//
//    @Bean
//    public PlatformTransactionManager transactionManager(DataSource dataSource) {
//        JpaTransactionManager txManager = new JpaTransactionManager();
//        txManager.setEntityManagerFactory(entityManagerFactory());
//        return txManager;
//    }
//
//    @Bean
//    public EventStorageEngine eventStorageEngine() {
//        return new InMemoryEventStorageEngine();
//    }
//
//    @Bean
//    public TransactionManager axonTransactionManager(DataSource dataSource) {
//        return new SpringTransactionManager(transactionManager(dataSource));
//    }
//
//    @Bean
//    public EventBus eventBus() {
//        return new SimpleEventBus.Builder().build();
//    }
//
//    @Bean
//    public CommandBus commandBus(DataSource dataSource) {
//        return new SimpleCommandBus.Builder().transactionManager(axonTransactionManager(dataSource)).build();
//    }
//
//    @Bean
//    public TransactionManagingInterceptor transactionManagingInterceptor(DataSource dataSource) {
//        return new TransactionManagingInterceptor(new SpringTransactionManager(transactionManager));
//    }
//
//    @Bean
//    public EntityManagerProvider entityManagerProvider(EntityManager entityManager) {
//        return new SimpleEntityManagerProvider(entityManager);
//    }
//
//    @Bean
//    public Repository<BankAccount> accountRepository(EntityManager entityManager) {
//        return GenericJpaRepository.builder(BankAccount.class)
//            .entityManagerProvider(entityManagerProvider(entityManager)).eventBus(eventBus()).build();
//    }
//
//    @Bean
//    public EventProcessingModule eventProcessingModule() {
//        return new EventProcessingModule();
//    }
}
