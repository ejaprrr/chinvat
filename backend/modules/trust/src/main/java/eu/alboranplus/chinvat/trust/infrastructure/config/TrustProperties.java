package eu.alboranplus.chinvat.trust.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "chinvat.trust")
public class TrustProperties {

  private String euLotlUrl = "https://ec.europa.eu/tools/lotl/eu-lotl.xml";
  private int connectTimeoutMillis = 5000;
  private int readTimeoutMillis = 15000;
  private boolean debug = false;

  public String getEuLotlUrl() {
    return euLotlUrl;
  }

  public void setEuLotlUrl(String euLotlUrl) {
    this.euLotlUrl = euLotlUrl;
  }

  public int getConnectTimeoutMillis() {
    return connectTimeoutMillis;
  }

  public void setConnectTimeoutMillis(int connectTimeoutMillis) {
    this.connectTimeoutMillis = connectTimeoutMillis;
  }

  public int getReadTimeoutMillis() {
    return readTimeoutMillis;
  }

  public void setReadTimeoutMillis(int readTimeoutMillis) {
    this.readTimeoutMillis = readTimeoutMillis;
  }

  public boolean isDebug() {
    return debug;
  }

  public void setDebug(boolean debug) {
    this.debug = debug;
  }
}
