package eu.alboranplus.chinvat.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Konfigurační vlastnosti pro database connection pooling.
 * Mapuje se z application.properties do spring.datasource.hikari prefix.
 */
@ConfigurationProperties(prefix = "spring.datasource.hikari")
public class DatabaseProperties {
  private String url;
  private String username;
  private String password;
  private int maximumPoolSize = 10;
  private int minimumIdle = 2;
  private long connectionTimeout = 30000L;
  private long idleTimeout = 600000L;
  private long maxLifetime = 1800000L;
  private boolean autoCommit = true;
  private long leakDetectionThreshold = 15000L;

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public int getMaximumPoolSize() {
    return maximumPoolSize;
  }

  public void setMaximumPoolSize(int maximumPoolSize) {
    this.maximumPoolSize = maximumPoolSize;
  }

  public int getMinimumIdle() {
    return minimumIdle;
  }

  public void setMinimumIdle(int minimumIdle) {
    this.minimumIdle = minimumIdle;
  }

  public long getConnectionTimeout() {
    return connectionTimeout;
  }

  public void setConnectionTimeout(long connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
  }

  public long getIdleTimeout() {
    return idleTimeout;
  }

  public void setIdleTimeout(long idleTimeout) {
    this.idleTimeout = idleTimeout;
  }

  public long getMaxLifetime() {
    return maxLifetime;
  }

  public void setMaxLifetime(long maxLifetime) {
    this.maxLifetime = maxLifetime;
  }

  public boolean isAutoCommit() {
    return autoCommit;
  }

  public void setAutoCommit(boolean autoCommit) {
    this.autoCommit = autoCommit;
  }

  public long getLeakDetectionThreshold() {
    return leakDetectionThreshold;
  }

  public void setLeakDetectionThreshold(long leakDetectionThreshold) {
    this.leakDetectionThreshold = leakDetectionThreshold;
  }
}
