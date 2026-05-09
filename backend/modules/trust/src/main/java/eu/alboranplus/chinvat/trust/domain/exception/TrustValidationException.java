package eu.alboranplus.chinvat.trust.domain.exception;

public class TrustValidationException extends RuntimeException {
  public TrustValidationException(String message) {
    super(message);
  }

  public TrustValidationException(String message, Throwable cause) {
    super(message, cause);
  }
}