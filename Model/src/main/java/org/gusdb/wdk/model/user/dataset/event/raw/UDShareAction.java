package org.gusdb.wdk.model.user.dataset.event.raw;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UDShareAction
{
  GRANT,
  REVOKE;

  @JsonCreator
  public static UDShareAction fromString(String val) {
    switch (val) {
      case "grant":
        return GRANT;
      case "revoke":
        return REVOKE;
      default:
        throw new IllegalArgumentException();
    }
  }

  @JsonValue
  public String externalValue() {
    return name().toLowerCase();
  }
}
