package eu.alboranplus.chinvat.eidas.domain.exception;

public class EidasProviderNotFoundException extends RuntimeException {
  public EidasProviderNotFoundException(String message) {
    super(message);
  }
}
