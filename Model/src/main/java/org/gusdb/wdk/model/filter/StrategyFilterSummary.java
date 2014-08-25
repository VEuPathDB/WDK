package org.gusdb.wdk.model.filter;

import java.util.Collection;

import org.gusdb.wdk.model.user.Strategy;

public class StrategyFilterSummary implements FilterSummary {

  private final Collection<Strategy> strategies;
  
  public StrategyFilterSummary(Collection<Strategy> strategies) {
    this.strategies = strategies;
  }

  public Collection<Strategy> getStrategies() {
    return strategies;
  }
}
