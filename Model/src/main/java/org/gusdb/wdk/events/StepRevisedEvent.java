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
  private final Step _previousVersion;

  public StepRevisedEvent(Step revisedStep, Step previousVersion) {
    _revisedStep = revisedStep;
    _previousVersion = previousVersion;
  }

  public Step getRevisedStep() {
    return _revisedStep;
  }

  public Step getPreviousVersion() {
    return _previousVersion;
  }
}
