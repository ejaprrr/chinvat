package eu.alboranplus.chinvat.users;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/** Minimal Spring Boot application to bootstrap the users module JPA context for tests. */
@ComponentScan(basePackages = {"eu.alboranplus.chinvat.users", "eu.alboranplus.chinvat.common"})
@EnableJpaRepositories(basePackages = {"eu.alboranplus.chinvat.users", "eu.alboranplus.chinvat.common"})
@SpringBootApplication(scanBasePackages = {"eu.alboranplus.chinvat.users", "eu.alboranplus.chinvat.common"})
public class UsersTestApplication {}
