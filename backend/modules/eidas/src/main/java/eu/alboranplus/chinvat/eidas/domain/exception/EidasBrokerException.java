package eu.alboranplus.chinvat.eidas.domain.exception;

public class EidasBrokerException extends RuntimeException {
  public EidasBrokerException(String message) {
    super(message);
  }

  public EidasBrokerException(String message, Throwable cause) {
    super(message, cause);
  }
}