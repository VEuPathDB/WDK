package org.gusdb.wdk.model.bundle.filter.config;

import org.gusdb.wdk.model.bundle.filter.FilterComparator;

public class ComparableConfig<T> {
  private T value;
  private FilterComparator comparator;

  public T getValue() {
    return value;
  }

  public ComparableConfig<T> setValue(T value) {
    this.value = value;
    return this;
  }

  public FilterComparator getComparator() {
    return comparator;
  }

  public ComparableConfig<T> setComparator(FilterComparator comparator) {
    this.comparator = comparator;
    return this;
  }

  public ComparableConfig<T> copy() {
    return new ComparableConfig<T>()
      .setValue(value)
      .setComparator(comparator);
  }

  @Override
  public String toString() {
    return "ComparableConfig{"
      + "value="
      + value
      + ", comparator="
      + comparator
      + '}';
  }
}
