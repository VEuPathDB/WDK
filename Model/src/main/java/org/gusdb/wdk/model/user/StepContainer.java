package org.gusdb.wdk.model.user;

import java.util.function.Predicate;

import org.gusdb.fgputil.Tuples.TwoTuple;

public interface StepContainer {

  public static class StepSearch extends TwoTuple<Predicate<Step>, String> {
    public StepSearch(Predicate<Step> predicate, String description) {
      super(predicate, description);
    }
    public Predicate<Step> getPredicate() { return getFirst(); }
    public String getDescription() { return getSecond(); }
  }

  public static StepSearch withId(long stepId) {
    return new StepSearch(step -> step.getStepId() == stepId, "with ID = " + stepId);
  }

  public static StepSearch parentOf(long stepId) {
    return new StepSearch(step -> step.getChildStepId() == stepId || step.getPreviousStepId() == stepId, " that is the parent of " + stepId);
  }

  public static StepContainer emptyContainer() {
    return new StepContainer(){};
  }

  public default Step findStep(StepSearch search) {
    throw new IllegalArgumentException("This container does not contain a step " + search.getDescription());
  }

}
