package org.gusdb.wdk.model.user.dataset.event.model;

public enum UserDatasetEventType
{
  INSTALL,
  UNINSTALL,
  SHARE;

  public String internalValue() {
    return name().toLowerCase();
  }

  @Override
  public String toString() {
    return internalValue();
  }

  public static UserDatasetEventType fromString(String value) {
    if (value != null) {
      value = value.toUpperCase();

      for (var en : values())
        if (en.name().equals(value))
          return en;
    }

    throw new IllegalArgumentException();
  }
}
