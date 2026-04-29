package eu.alboranplus.chinvat.rbac.domain.exception;

public class PermissionNotFoundException extends RuntimeException {

  public PermissionNotFoundException(String message) {
    super(message);
  }
}
