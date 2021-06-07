package org.gusdb.wdk.model.user.dataset.event.raw;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UDEventType
{
  INSTALL,
  SHARE,
  UNINSTALL;

  @JsonCreator
  public static UDEventType fromString(String value) {
    switch (value) {
      case "install":
        return INSTALL;
      case "share":
        return SHARE;
      case "uninstall":
        return UNINSTALL;
      default:
        throw new IllegalArgumentException();
    }
  }

  @JsonValue
  public String externalValue() {
    return name().toLowerCase();
  }
}
