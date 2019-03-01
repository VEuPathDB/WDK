package org.gusdb.wdk.model.filter;

import java.util.Map;

import org.json.JSONObject;

public class ListColumnFilterSummary {

  private final Map<String, Integer> _counts;
  private final int _maxCount;

  public ListColumnFilterSummary(Map<String, Integer> counts) {
    _counts = counts;
    int max = Integer.MIN_VALUE;
    for (int count : counts.values()) {
      if (count > max)
        max = count;
    }
    _maxCount = max;
  }

  public Map<String, Integer> getCounts() {
    return _counts;
  }

  public int getMaxCount() {
    return _maxCount;
  }

  public JSONObject toJson() {
    return new JSONObject().put("maxCount", _maxCount).put("counts", _counts);
  }
}
