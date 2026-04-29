package eu.alboranplus.chinvat.auth.domain.model;

public enum AuthSessionTokenKind {
  ACCESS("A"),
  REFRESH("R");

  private final String tokenPrefix;

  AuthSessionTokenKind(String tokenPrefix) {
    this.tokenPrefix = tokenPrefix;
  }

  public String tokenPrefix() {
    return tokenPrefix;
  }
}

