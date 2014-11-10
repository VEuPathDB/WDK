package org.gusdb.wdk.model.filter;

import java.util.Map;

public class ListColumnFilterSummary implements FilterSummary {

  private final Map<String, Integer> _counts;
  private final int _maxCount;

  public ListColumnFilterSummary(Map<String, Integer> counts) {
    this._counts = counts;
    int max = Integer.MIN_VALUE;
    for (int count : counts.values()) {
      if (count > max)
        count = max;
    }
    this._maxCount = max;
  }

  public Map<String, Integer> getCounts() {
    return _counts;
  }

  public int getMaxCount() {
    return _maxCount;
  }
}
