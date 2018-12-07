package org.gusdb.wdk.model.user;

import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.factory.AnswerValueFactory;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.answer.spec.AnswerSpecBuilder;
import org.gusdb.wdk.model.answer.spec.FilterOptionList;
import org.gusdb.wdk.model.question.Question;

/**
 * Manages AnswerValues for the Step class
 * 
 * @author rdoherty
 */
public class AnswerValueCache {

  private static final Logger LOG = Logger.getLogger(AnswerValueCache.class);

  // Step object this cache is for
  private final RunnableObj<Step> _step;

  // record range for this page
  private int[] _range;

  // AnswerValues for this step (first is validated answer, second is unvalidated answer)
  private TwoTuple<AnswerValue, AnswerValue> _answerValues = new TwoTuple<>(null, null);

  // View AnswerValues for this step  (first is validated answer, second is unvalidated answer)
  //    (may be different than answerValue IFF viewOnlyFilters are present)
  private TwoTuple<AnswerValue, AnswerValue> _viewAnswerValues = new TwoTuple<>(null, null);

  public AnswerValueCache(RunnableObj<Step> step) {
    _step = step;
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
      throws WdkModelException {
    _answerValues = getAnswerValuePair(_answerValues, validate, false);
    return (validate ? _answerValues.getFirst() : _answerValues.getSecond());
  }

  public AnswerValue getViewAnswerValue(boolean validate)
      throws WdkModelException {
    if (_step.getObject().getAnswerSpec().getViewFilterOptions().getSize() == 0) {
      return getAnswerValue(validate);
    }
    _viewAnswerValues = getAnswerValuePair(_viewAnswerValues, validate, true);
    return (validate ? _viewAnswerValues.getFirst() : _viewAnswerValues.getSecond());
  }

  private TwoTuple<AnswerValue, AnswerValue> getAnswerValuePair(
      TwoTuple<AnswerValue, AnswerValue> currentValues,
      boolean validate, boolean applyViewFilters)
          throws WdkModelException {
    AnswerValue validated = currentValues.getFirst();
    AnswerValue unvalidated = currentValues.getSecond();
    if (validate) {
      if (validated != null) {
        return currentValues;
      }
      else {
        validated = makeAnswerValue(_step, getRange(), true, applyViewFilters);
        unvalidated = validated;
      }
    }
    else {
      if (unvalidated == null) {
        unvalidated = makeAnswerValue(_step, getRange(), false, applyViewFilters);
      }
    }
    return new TwoTuple<>(validated, unvalidated);
  }

  private int[] getRange() {
    if (_range == null) {
      // expandStep script generates step in memory with step_id 0 and user_id 0
      _range = _step.getObject().getStepId() == 0 ? new int[]{1, 20} : getDefaultPageRange(_step.getObject().getUser());
    }
    return _range;
  }

  private static int[] getDefaultPageRange(User user) {
    return new int[]{ 1, user.getPreferences().getItemsPerPage() };
  }

  private static AnswerValue makeAnswerValue(RunnableObj<Step> runnableStep, int[] range, boolean validate, boolean applyViewFilters)
      throws WdkModelException {
    Step step = runnableStep.getObject();
    Question question = step.getAnswerSpec().getQuestion();
    User user = step.getUser();
    Map<String, Boolean> sortingMap = user.getPreferences().getSortingAttributes(
        question.getFullName(), UserPreferences.DEFAULT_SUMMARY_VIEW_PREF_SUFFIX);
    AnswerSpecBuilder answerSpec = AnswerSpec.builder(step.getAnswerSpec());
    if (!applyViewFilters) {
      answerSpec.setViewFilterOptions(FilterOptionList.builder()); // clear any view filters
    }
    AnswerValue answerValue = AnswerValueFactory.makeAnswer(user,
        answerSpec.buildRunnable(user, step.getContainer()), range[0], range[1], sortingMap);
    try {
      int displayResultSize = answerValue.getResultSizeFactory().getDisplayResultSize();
      if (!applyViewFilters) {
        // saves updated estimate size
        step.updateEstimatedSize(displayResultSize);
        step.writeMetadataToDb(false);
      }
    }
    catch (WdkModelException ex) {
      // if validate is false, the error will be ignored to allow the process to continue.
      if (validate)
        throw ex;
      else
        LOG.warn(ex);
    }
    return answerValue;
  }
}
