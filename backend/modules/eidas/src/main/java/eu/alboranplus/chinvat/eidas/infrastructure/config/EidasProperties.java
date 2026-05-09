package eu.alboranplus.chinvat.eidas.infrastructure.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "chinvat.eidas")
public class EidasProperties {

  private String brokerBaseUrl = "https://eidas-broker.example.invalid";
  private String brokerProvidersPath = "/api/v1/providers";
  private String brokerLoginPath = "/api/v1/login";
  private String brokerCallbackPath = "/api/v1/callback";
  private int brokerConnectTimeoutMillis = 5000;
  private Duration stateTtl = Duration.ofMinutes(10);
  private String stateKeyPrefix = "chinvat:eidas:state:";
  private StateRedis stateRedis = new StateRedis();
  private List<Provider> providers = new ArrayList<>(Collections.singletonList(new Provider()));

  public String getBrokerBaseUrl() {
    return brokerBaseUrl;
  }

  public void setBrokerBaseUrl(String brokerBaseUrl) {
    this.brokerBaseUrl = brokerBaseUrl;
  }

  public String getBrokerProvidersPath() {
    return brokerProvidersPath;
  }

  public void setBrokerProvidersPath(String brokerProvidersPath) {
    this.brokerProvidersPath = brokerProvidersPath;
  }

  public String getBrokerLoginPath() {
    return brokerLoginPath;
  }

  public void setBrokerLoginPath(String brokerLoginPath) {
    this.brokerLoginPath = brokerLoginPath;
  }

  public String getBrokerCallbackPath() {
    return brokerCallbackPath;
  }

  public void setBrokerCallbackPath(String brokerCallbackPath) {
    this.brokerCallbackPath = brokerCallbackPath;
  }

  public int getBrokerConnectTimeoutMillis() {
    return brokerConnectTimeoutMillis;
  }

  public void setBrokerConnectTimeoutMillis(int brokerConnectTimeoutMillis) {
    this.brokerConnectTimeoutMillis = brokerConnectTimeoutMillis;
  }

  public Duration getStateTtl() {
    return stateTtl;
  }

  public void setStateTtl(Duration stateTtl) {
    this.stateTtl = stateTtl;
  }

  public String getStateKeyPrefix() {
    return stateKeyPrefix;
  }

  public void setStateKeyPrefix(String stateKeyPrefix) {
    this.stateKeyPrefix = stateKeyPrefix;
  }

  public StateRedis getStateRedis() {
    return stateRedis;
  }

  public void setStateRedis(StateRedis stateRedis) {
    this.stateRedis = stateRedis;
  }

  public List<Provider> getProviders() {
    return providers;
  }

  public void setProviders(List<Provider> providers) {
    this.providers = providers;
  }

  public static class Provider {
    private String code = "EIDAS_EU";
    private String displayName = "eIDAS EU";
    private String countryCode = "EU";
    private boolean enabled = true;

    public String getCode() {
      return code;
    }

    public void setCode(String code) {
      this.code = code;
    }

    public String getDisplayName() {
      return displayName;
    }

    public void setDisplayName(String displayName) {
      this.displayName = displayName;
    }

    public String getCountryCode() {
      return countryCode;
    }

    public void setCountryCode(String countryCode) {
      this.countryCode = countryCode;
    }

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }
  }

  public static class StateRedis {
    private boolean dedicated = false;
    private String host = "localhost";
    private int port = 6379;
    private int database = 1;
    private String username;
    private String password;

    public boolean isDedicated() {
      return dedicated;
    }

    public void setDedicated(boolean dedicated) {
      this.dedicated = dedicated;
    }

    public String getHost() {
      return host;
    }

    public void setHost(String host) {
      this.host = host;
    }

    public int getPort() {
      return port;
    }

    public void setPort(int port) {
      this.port = port;
    }

    public int getDatabase() {
      return database;
    }

    public void setDatabase(int database) {
      this.database = database;
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
  }
}
