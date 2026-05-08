package eu.alboranplus.chinvat.common.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Centrální konfigurace databáze pro všechny moduly aplikace.
 * Konfiguruje transakční management a JPA.
 * 
 * <p>DataSource se konfiguruje automaticky přes Spring Boot DataSourceAutoConfiguration
 * s HikariCP properties v application.properties.
 */
@Configuration
public class DatabaseConfiguration {

  /**
   * Konfiguruje JPA transaction manager pro managed transactional operations.
   */
  @Bean
  public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
    return new JpaTransactionManager(entityManagerFactory);
  }
}
