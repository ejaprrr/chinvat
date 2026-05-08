package eu.alboranplus.chinvat.common.audit;

import java.util.LinkedHashMap;
import java.util.Map;

public final class AuditDetails {

  private AuditDetails() {}

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private final Map<String, Object> values = new LinkedHashMap<>();

    private Builder() {}

    public Builder add(String key, Object value) {
      if (value != null) {
        values.put(key, value);
      }
      return this;
    }

    public Map<String, Object> build() {
      return Map.copyOf(values);
    }
  }
}