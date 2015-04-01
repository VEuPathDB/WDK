package org.gusdb.wdk.events;

import java.util.List;

import org.gusdb.fgputil.events.Event;

/**
 * This event will be fired when a set of steps have been modified (i.e. their
 * checksums have changed, so their results are out of date).  When a step is
 * revised, any steps that rely on it are also "modified".  This event wraps
 * the full set of modified steps.  Any step-dependent objects should listen
 * for this event and react as necessary.
 * 
 * @author ryan
 */
public class StepsModifiedEvent extends Event {

  private List<Integer> _stepIds;

  public StepsModifiedEvent(List<Integer> stepIds) {
    _stepIds = stepIds;
  }

  public List<Integer> getStepIds() {
    return _stepIds;
  }
}
