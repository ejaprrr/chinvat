package eu.alboranplus.chinvat.auth;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/** Minimal Spring Boot application to bootstrap the auth module JPA context for tests. */
@ComponentScan(basePackages = {"eu.alboranplus.chinvat.auth", "eu.alboranplus.chinvat.common"})
@EnableJpaRepositories(basePackages = {"eu.alboranplus.chinvat.auth", "eu.alboranplus.chinvat.common"})
@SpringBootApplication(scanBasePackages = {"eu.alboranplus.chinvat.auth", "eu.alboranplus.chinvat.common"})
public class AuthTestApplication {}
