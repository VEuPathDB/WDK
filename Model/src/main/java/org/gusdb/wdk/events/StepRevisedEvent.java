package org.gusdb.wdk.events;

import org.gusdb.fgputil.events.Event;
import org.gusdb.wdk.model.user.Step;

public class StepRevisedEvent extends Event {

  private final Step _revisedStep;

  public StepRevisedEvent(Step revisedStep) {
    _revisedStep = revisedStep;
  }

  public Step getRevisedStep() {
    return _revisedStep;
  }
}
