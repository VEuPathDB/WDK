package org.gusdb.wdk.model.user;

import java.util.Optional;
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

  /**
   * Tries to find the first step in this container that passes the given search criteria
   * 
   * @param search search criteria
   * @return An optional containing the first matching step, or an empty optional if not found
   */
  public default Optional<Step> findFirstStep(StepSearch search) {
    return Optional.empty();
  }

  /**
   * Tries to find the first step in this container that passes the given search criteria and throws
   * NoSuchElementException if no step matches the criteria given.
   * 
   * @param search search criteria
   * @return the first matching step
   * @throws NoSuchElementException if no step matches
   */
  public default Step findFirstStepOrThrow(StepSearch search) throws NoSuchElementException {
    return findFirstStep(search).orElseThrow(() -> // throws exception with the proper message
        new NoSuchElementException("This container does not contain a step " + search.getDescription()));
  }

}
