package org.gusdb.wdk.events;

import org.gusdb.fgputil.events.Event;
import org.gusdb.wdk.model.user.Step;

public class WdkEvents {
  
  private WdkEvents() { /* static class */ }

  public static class StepRevisedEvent extends Event {
    private final Step _revisedStep;
    public StepRevisedEvent(Step revisedStep) {
      _revisedStep = revisedStep;
    }
    public Step getRevisedStep() {
      return _revisedStep;
    }
  }

  public static class StepCopiedEvent extends Event {
    private final Step _fromStep, _toStep;
    public StepCopiedEvent(Step fromStep, Step toStep) {
      _fromStep = fromStep;
      _toStep = toStep;
    }
    public Step getFromStep() {
      return _fromStep;
    }
    public Step getToStep() {
      return _toStep;
    }
  }
}
