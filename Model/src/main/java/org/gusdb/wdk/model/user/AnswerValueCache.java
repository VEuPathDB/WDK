package org.gusdb.wdk.model.user;

import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.question.Question;

/**
 * Manages AnswerValues for the Step class
 * 
 * @author rdoherty
 */
public class AnswerValueCache {

  private static final Logger LOG = Logger.getLogger(AnswerValueCache.class);

  // Step object this cache is for
  private final Step _step;

  // record range for this page
  private int[] _range;

  // AnswerValues for this step (first is validated answer, second is unvalidated answer)
  private TwoTuple<AnswerValue, AnswerValue> _answerValues = new TwoTuple<>(null, null);

  // View AnswerValues for this step  (first is validated answer, second is unvalidated answer)
  //    (may be different than answerValue IFF viewOnlyFilters are present)
  private TwoTuple<AnswerValue, AnswerValue> _viewAnswerValues = new TwoTuple<>(null, null);

  public AnswerValueCache(Step step) throws WdkModelException {
    _step = step;
    _range = getDefaultPageRange(step.getUser());
  }

  public void invalidateAll() {
    _answerValues.set(null, null);
    _viewAnswerValues.set(null, null);
  }

  public void invalidateViewAnswers() {
    _viewAnswerValues.set(null, null);
  }

  public void setPaging(int start, int end) {
    _range = new int[]{ start, end };
    invalidateAll();
  }

  public AnswerValue getAnswerValue(boolean validate)
      throws WdkModelException, WdkUserException {
    _answerValues = getAnswerValuePair(_answerValues, _step, validate, false);
    return (validate ? _answerValues.getFirst() : _answerValues.getSecond());
  }

  public AnswerValue getViewAnswerValue(boolean validate)
      throws WdkModelException, WdkUserException {
    if (_step.getViewFilterOptions().getSize() == 0) {
      return getAnswerValue(validate);
    }
    _viewAnswerValues = getAnswerValuePair(_viewAnswerValues, _step, validate, true);
    return (validate ? _viewAnswerValues.getFirst() : _viewAnswerValues.getSecond());
  }

  private TwoTuple<AnswerValue, AnswerValue> getAnswerValuePair(
      TwoTuple<AnswerValue, AnswerValue> currentValues, Step step,
      boolean validate, boolean applyViewFilters)
          throws WdkModelException, WdkUserException {
    AnswerValue validated = currentValues.getFirst();
    AnswerValue unvalidated = currentValues.getSecond();
    if (validate) {
      if (validated != null) {
        return currentValues;
      }
      else {
        validated = makeAnswerValue(_step, _range, true, applyViewFilters);
        unvalidated = validated;
      }
    }
    else {
      if (unvalidated == null) {
        unvalidated = makeAnswerValue(_step, _range, false, applyViewFilters);
      }
    }
    return new TwoTuple<>(validated, unvalidated);
  }

  private static int[] getDefaultPageRange(User user) {
    return new int[]{ 1, user.getItemsPerPage() };
  }
  
  private static AnswerValue makeAnswerValue(Step step, int[] range, boolean validate, boolean applyViewFilters)
      throws WdkModelException, WdkUserException {
    Question question = step.getQuestion();
    User user = step.getUser();
    Map<String, Boolean> sortingMap = user.getSortingAttributes(question.getFullName());
    AnswerValue answerValue = question.makeAnswerValue(user, step.getParamValues(), range[0],
        range[1], sortingMap, step.getFilter(), validate, step.getAssignedWeight());
    answerValue.setFilterOptions(step.getFilterOptions());
    if (applyViewFilters) {
      answerValue.setViewFilterOptions(step.getViewFilterOptions());
    }
    try {
      int displayResultSize = answerValue.getDisplayResultSize();
      if (!applyViewFilters) {
        // saves updated estimate size
        step.setEstimateSize(displayResultSize);
        step.update(false);
      }
    }
    catch (WdkModelException | WdkUserException ex) {
      // if validate is false, the error will be ignored to allow the process to continue.
      if (validate)
        throw ex;
      else
        LOG.warn(ex);
    }
    return answerValue;
  }
}
