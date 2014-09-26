package org.gusdb.wdk.model.filter;

import java.util.Map;

public class ListColumnFilterSummary implements FilterSummary {

  private final Map<String, Integer> counts;
  
  public ListColumnFilterSummary(Map<String, Integer> counts) {
    this.counts = counts;
  }

  public Map<String, Integer> getCounts() {
    return counts;
  }
}
