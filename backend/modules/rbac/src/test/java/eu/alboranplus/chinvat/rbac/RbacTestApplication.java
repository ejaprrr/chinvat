package eu.alboranplus.chinvat.rbac;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/** Minimal Spring Boot application to bootstrap the RBAC module JPA context for tests. */
@ComponentScan(basePackages = {"eu.alboranplus.chinvat.rbac", "eu.alboranplus.chinvat.common"})
@EnableJpaRepositories(basePackages = {"eu.alboranplus.chinvat.rbac", "eu.alboranplus.chinvat.common"})
@SpringBootApplication(scanBasePackages = {"eu.alboranplus.chinvat.rbac", "eu.alboranplus.chinvat.common"})
public class RbacTestApplication {}
