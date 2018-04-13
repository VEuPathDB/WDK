package org.gusdb.wdk.model.filter;

import org.gusdb.wdk.model.query.Query;

/**
 * This is a contract to the steps that are applied to the step directly.
 * 
 * @author Jerric
 *
 */
public abstract class StepFilter extends AbstractFilter {

  private Query _summaryQuery = null;

  public Query getSummaryQuery() {
    return _summaryQuery;
  }

  public void setSummaryQuery(Query summaryQuery) {
    _summaryQuery = summaryQuery;
  }

}
