package org.gusdb.wdk.model.user;

import static org.gusdb.wdk.model.user.StepContainer.withId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;

public class StrategyOperations {

  /**
   * Delete the given step from the strategy. Additional step, or even the current strategy maybe deleted,
   * depending on the following cases:
   *
   * #1 - if there is only one step in the strategy and it's deleted, then the whole strategy will be deleted.
   *
   * #2 - if the step to be deleted is a combined one, all the steps in its child branch will also be deleted.
   *
   * #3 - if the step has previous and next, the previous will be connected to the next;
   *
   * #2 & #3 means that when deleting a nested step in the main strategy, we should send the id of the boolean
   * that takes the nested step, and it will delete both boolean and the whole nested strategy; however, if
   * the root of the nested strategy is passed in, the only the root will be deleted, and the remaining steps
   * in the nested strategy will become a new sub-tree.
   *
   * @param step
   * @return a map of root id changes (both main & nested strategies) {old, new}; the information will be used
   *         to update the states of active strategies.
   * @throws WdkModelException
   * @throws WdkUserException
   */
  /* previously part of Strategy.java
  public Map<Long, Long> deleteStep(Step step) throws WdkModelException, WdkUserException {
    StepFactory stepFactory = _wdkModel.getStepFactory();
    List<Step> deletes = new ArrayList<>();
    Map<Long, Long> rootMap = new HashMap<>();

    // if the strategy is saved, need to make a unsaved copy first
    if (isSaved())
      writeMetadataToDb(false);

    // if a step has child, delete all the steps on that branch.
    Step childStep = step.getChildStep();
    if (childStep != null)
      deletes.addAll(childStep.getNestedBranch());
    deletes.add(step); // delete the current step

    // keep a reference to previous step, in case we need to delete multiple steps. this previousStep will be
    // used to connect to the remaining steps.
    Step previousStep = step.getPreviousStep();

    // loop while the current step is marked to be deleted.
    while (step != null && deletes.contains(step)) {
      Step nextStep = getNext(step);
      if (nextStep != null) { // go to the next step in the same panel.
        step = nextStep;
        if (previousStep == null) {
          if (step.isCombined() && !step.isTransform()) {
            // a two-input combined step, since there is no previousStep, the child of it will become new
            // previousStep, and this combined step will be deleted
            previousStep = step.getChildStep();
          } // otherwise, a transform step, and since there is no previousStep, it will also be deleted
          deletes.add(step);
        }
        else { // otherwise, previous step exists, no more deletion needed. will exit loop.
          // check if the step can take the previous step
          stepFactory.dropDependency(previousStep.getStepId(), StepFactoryHelpers.COLUMN_LEFT_CHILD_ID);
          step.checkPreviousAllowed(previousStep);
          step.setPreviousStep(previousStep);
          step.writeParamFiltersToDb();
        }
      }
      else { // no next step exists, the current step must be a root of main/nested strategy.
        Step parentStep = getParent(step);
        if (parentStep != null) { // found a parent, which means we are deleting in a nested strategy.
          if (previousStep != null) { // make the previous step in the nested strategy a new root there.
            // get the collapsing info from old root of the nested strategy.
            previousStep.setCollapsible(step.isCollapsible());
            previousStep.setCollapsedName(step.getCollapsedName());
            previousStep.writeMetadataToDb(false);
            rootMap.put(step.getStepId(), previousStep.getStepId());

            // check if parent can take previous step as child
            stepFactory.dropDependency(previousStep.getStepId(), StepFactoryHelpers.COLUMN_RIGHT_CHILD_ID);
            parentStep.checkChildAllowed(previousStep);
            parentStep.setChildStep(previousStep);
            parentStep.writeParamFiltersToDb();
            break;
          }
          else { // no previousStep
            // Otherwise, the last step from nested strategy is deleted, we will also delete the parent; but
            // now the previousStep will become the previous one from the parent.
            previousStep = parentStep.getPreviousStep();
            deletes.add(parentStep);
          }
        } // otherwise, we are deleting the last step in main branch, will handle it outside of the loop
        step = parentStep;
      }
    }

    if (step == null) {
      if (previousStep != null) { // current step is null, then previous step should become new root.
        rootMap.put(getRootStepId(), previousStep.getStepId());
        setRootStep(previousStep);
        writeMetadataToDb(false);
      }
      else { // no more steps left in the strategy, delete the strategy itself.
        stepFactory.deleteStrategy(_strategyId);
        rootMap.clear();
      }
    }

    // after strategy is deleted (if needed), will now delete steps
    LOG.debug("Total " + deletes.size() + " steps deleted.");
    for (Step delete : deletes) {
      stepFactory.deleteStep(delete.getStepId());
    }

    return rootMap;
  }*/

  /**
   * Inserting a step after the target. This is used when we add steps in main and nested strategy. The
   * newStep will become the next step of the target.
   *
   * @param newStep
   *          The next step has to be a combined step, with the target as the previous step of it.
   * @param targetId
   *          a target step id that can live anywhere in the step tree.
   * @return a map of oldStepId to newStepId that are roots of the strategy or nested strategy.
   * @throws WdkModelException
   * @throws WdkUserException
   */
  /* previously part of Strategy.java
  public Map<Long, Long> insertStepAfter(Step newStep, long targetId) throws WdkModelException,
      WdkUserException {
    Map<Long, Long> rootMap = new HashMap<>();

    // make sure the newStep uses target step as its previousStep
    Step previousStep = newStep.getPreviousStep();
    if (previousStep == null || previousStep.getStepId() != targetId)
      throw new WdkUserException("Cannot insert step #" + newStep.getStepId() + " after step #" + targetId +
          " since it will corrupt the structure of the strategy #" + _strategyId);

    // if the strategy is saved, need to make a unsaved copy first
    if (isSaved())
      writeMetadataToDb(false);

    Step targetStep = findFirstStep(withId(targetId))
        .orElseThrow(() -> new WdkModelException("Could not find target step."));

    Step nextStep = getNext(targetStep);
    if (nextStep != null) { // insert in the middle of a strategy
      // make sure the next step can take the newStep as previousStep
      nextStep.checkPreviousAllowed(newStep);
      // link new step to target step.
      targetStep.setNextStep(newStep);
      // link next step to the new step
      nextStep.setPreviousStep(newStep);
      nextStep.writeParamFiltersToDb();
    }
    else { // newStep will be the last one in main/nested strategy
      Step parentStep = getParent(targetStep);
      if (parentStep != null) {
        // make sure the parent step can take the newStep as childStep
        parentStep.checkChildAllowed(newStep);
      }

      // copy over collapsing info
      if (previousStep.isCollapsible()) {
        newStep.setCollapsible(previousStep.isCollapsible());
        newStep.setCollapsedName(previousStep.getCollapsedName());
        newStep.writeMetadataToDb(false);

        // reset the colllapsing infom
        previousStep.setCollapsible(false);
        previousStep.setCollapsedName(null);
        previousStep.writeMetadataToDb(false);
      }

      rootMap.put(previousStep.getStepId(), newStep.getStepId());

      if (parentStep != null) {
        // a step is inserted at the end of a nested strategy -- ie. add step on nested strategy.
        // the new step will become the child of the parentStep
        parentStep.setChildStep(newStep);
        parentStep.writeParamFiltersToDb();
      }
      else { // a step is inserted at the end of main strategy
        setRootStep(newStep);
        writeMetadataToDb(false);
      }
    }
    return rootMap;
  }*/

  /**
   * Insert a new step before the target. The new step will become the previous step of the target, and the
   * old previousStep of the target should become the previousStep of the new step.
   *
   * @param newStep
   *          the newStep
   * @param targetId
   * @throws WdkModelException
   * @throws WdkUserException
   */
  /* previously part of Strategy.java
  public Map<Long, Long> insertStepBefore(Step newStep, long targetId) throws WdkModelException,
      WdkUserException {
    Step targetStep = findFirstStep(withId(targetId)).get();
    verifySameOwnerAndProject(this, targetStep);

    Map<Long, Long> rootMap = new HashMap<>();

    // if the strategy is saved, need to make a unsaved copy first
    if (isSaved())
      writeMetadataToDb(false);

    // make sure the previousStep of the target is now the previousStep of newStep
    if (targetStep.isFirstStep()) { // inserting before first step will cause the first step being replaced by
      // the new step, while old first step will become the child of this new step
      if (newStep.getChildStep() == null || newStep.getChildStep().getStepId() != targetId)
        throw new WdkUserException("Cannot insert step #" + newStep.getStepId() + " before step #" +
            targetId + " since it will corrupt the structure of the strategy #" + _strategyId);

      // if the first step has any step upstream, will link it to the new step
      Step nextStep = getNext(targetStep);
      if (nextStep != null) { // insert the new step in the same strategy panel
        nextStep.checkPreviousAllowed(newStep);
        nextStep.setPreviousStep(newStep);
        nextStep.writeParamFiltersToDb();
      }
      else { // the target is the only step in the strategy/nested strategy.
        Step parentStep = getParent(targetStep);
        if (parentStep != null) {
          // add the new step as the last one in a nested strategy
          // copy information from target step
          newStep.setCollapsible(targetStep.isCollapsible());
          newStep.setCollapsedName(targetStep.getCollapsedName());
          newStep.writeMetadataToDb(false); // don't need to update LastRunTime
          targetStep.setCollapsible(false);
          targetStep.writeMetadataToDb(false);

          // check and set the newStep as the child of the parent, to replace the target step
          parentStep.checkChildAllowed(newStep);
          parentStep.setChildStep(newStep);
          parentStep.writeParamFiltersToDb();
          rootMap.put(targetId, newStep.getStepId());
        }
        else { // target is at the end of the strategy, set newStep as the end of the strategy
          setRootStep(newStep);
          writeMetadataToDb(false); // don't overwrite a saved strategy.
          rootMap.put(targetId, newStep.getStepId());
        }
      }
    }
    else { // target is not the first, then the previousStep of the target will become the previous of the
      // new step.
      if (targetStep.getPreviousStepId() != newStep.getPreviousStepId())
        throw new WdkUserException("Cannot insert step #" + newStep.getStepId() + " before step #" +
            targetId + " since it will corrupt the structure of the strategy #" + _strategyId);

      // make sure the target step can take the newStep as previousStep
      targetStep.checkPreviousAllowed(newStep);

      targetStep.setPreviousStep(newStep);
      targetStep.writeParamFiltersToDb();
    }
    return rootMap;
  }*/
}
