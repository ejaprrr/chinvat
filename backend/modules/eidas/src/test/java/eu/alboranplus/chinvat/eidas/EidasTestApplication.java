package eu.alboranplus.chinvat.eidas;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/** Minimal Spring Boot application to bootstrap the eIDAS module test context. */
@ComponentScan(basePackages = {"eu.alboranplus.chinvat.eidas"})
@EnableJpaRepositories(basePackages = {"eu.alboranplus.chinvat.eidas"})
@SpringBootApplication(scanBasePackages = {"eu.alboranplus.chinvat.eidas"})
public class EidasTestApplication {}