package org.gusdb.wdk.model.user.dataset.event.model;

public enum UserDatasetEventStatus
{
  PROCESSING,
  COMPLETE,
  FAILED,
  CLEANUP_READY,
  CLEANUP_PROCESSING,
  CLEANUP_COMPLETE,
  CLEANUP_FAILED;

  public String internalValue() {
    return name().toLowerCase();
  }

  public static UserDatasetEventStatus fromInternalValue(String value) {
    if (value != null) {
      value = value.toUpperCase();

      for (var v : values())
        if (v.name().equals(value))
          return v;
    }

    throw new IllegalArgumentException();
  }

  @Override
  public String toString() {
    return internalValue();
  }
}
