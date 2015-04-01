package org.gusdb.wdk.events;

import org.gusdb.fgputil.events.Event;
import org.gusdb.wdk.model.user.Step;

/**
 * This event will be fired when a step is revised by the user.  Any step-
 * dependent objects should listen for this event and react as necessary.
 * 
 * @author ryan
 */
public class StepRevisedEvent extends Event {

  private final Step _revisedStep;

  public StepRevisedEvent(Step revisedStep) {
    _revisedStep = revisedStep;
  }

  public Step getRevisedStep() {
    return _revisedStep;
  }
}
