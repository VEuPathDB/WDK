package org.gusdb.wdk.model.user;

/**
 * Simple interface declaring a class's instances related to a particular
 * strategy.  Thus methods are present for the element's ID and the ID of
 * the strategy it is associated with.
 * 
 * @author rdoherty
 */
public interface StrategyElement {

  long getId();
  Long getStrategyId();

}
