package org.gusdb.wdk.model.toolbundle.reporter.report;

import com.fasterxml.jackson.annotation.JsonProperty;

class Pair<T extends Comparable> implements Comparable<Pair<T>> {
  static final String
    KEY_VALUE = "value",
    KEY_COUNT = "count";

  @JsonProperty(KEY_VALUE)
  public T value;

  @JsonProperty(KEY_COUNT)
  public long count;

  public Pair(T value, long count) {
    this.value = value;
    this.count = count;
  }

  @Override
  @SuppressWarnings("unchecked")
  public int compareTo(Pair<T> o) {
    var x = Long.compare(count, o.count);
    return x == 0 ? o.value.compareTo(value) : x;
  }
}
