package eu.alboranplus.chinvat.rbac.domain.exception;

public class PermissionAlreadyExistsException extends RuntimeException {

  public PermissionAlreadyExistsException(String message) {
    super(message);
  }
}
