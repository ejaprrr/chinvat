package eu.alboranplus.chinvat.auth.domain.exception;

public class InvalidAuthenticationException extends RuntimeException {

  public InvalidAuthenticationException(String message) {
    super(message);
  }
}
