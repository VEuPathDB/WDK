package org.gusdb.wdk.model.user;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.SortDirection;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.json.JSONArray;
import org.json.JSONObject;

import static java.lang.Math.min;

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
    // expandStep script generates step in memory with step_id 0 and user_id 0
    if (step.getStepId() != 0) _range = getDefaultPageRange(step.getUser());
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
    _answerValues = getAnswerValuePair(_answerValues, validate, false);
    return (validate ? _answerValues.getFirst() : _answerValues.getSecond());
  }

  public AnswerValue getViewAnswerValue(boolean validate)
      throws WdkModelException, WdkUserException {
    if (_step.getViewFilterOptions().getSize() == 0) {
      return getAnswerValue(validate);
    }
    _viewAnswerValues = getAnswerValuePair(_viewAnswerValues, validate, true);
    return (validate ? _viewAnswerValues.getFirst() : _viewAnswerValues.getSecond());
  }

  private TwoTuple<AnswerValue, AnswerValue> getAnswerValuePair(
      TwoTuple<AnswerValue, AnswerValue> currentValues,
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
    return new int[]{ 1, user.getPreferences().getItemsPerPage() };
  }

  private static AnswerValue makeAnswerValue(Step step, int[] range, boolean validate, boolean applyViewFilters)
      throws WdkModelException, WdkUserException {
    Question question = step.getQuestion();
    User user = step.getUser();
    JSONObject obj = step.getDisplayPrefs();

    Map<String, Boolean> sortingMap = hasSortingMap(obj)
      ? parseSortingMap(obj, question.getAttributeFieldMap())
      : user.getPreferences().getSortingAttributes(question.getFullName(),
        UserPreferences.DEFAULT_SUMMARY_VIEW_PREF_SUFFIX);

    AnswerValue answerValue = question.makeAnswerValue(user, step.getParamValues(), range[0],
        range[1], sortingMap, step.getFilter(), validate, step.getAssignedWeight());
    answerValue.setFilterOptions(step.getFilterOptions());
    if (applyViewFilters) {
      answerValue.setViewFilterOptions(step.getViewFilterOptions());
    }

    if (hasColumnSelection(obj)) {
      answerValue.getAttributes()
        .overrideSummaryAttributeFieldMap(parseSummaryMap(obj,
          question.getAttributeFieldMap()));
    }

    try {
      int displayResultSize = answerValue.getResultSizeFactory().getDisplayResultSize();
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

  private static boolean hasSortingMap(JSONObject obj) {
    return Objects.nonNull(obj) && obj.has("sortColumns");
  }

  public static boolean hasColumnSelection(JSONObject obj) {
    return Objects.nonNull(obj) && obj.has("columnSelection");
  }

  private static Map<String, Boolean> parseSortingMap(JSONObject obj, Map<String, ?> valid) {
    final Map<String, Boolean> out = new LinkedHashMap<>();

    if (!hasSortingMap(obj))
      return out;

    JSONArray arr = obj.getJSONArray("sortColumns");
    for (int i = 0; i < min(arr.length(), UserPreferences.MAX_NUM_SORTING_COLUMNS); i++) {
      final JSONObject col = arr.getJSONObject(i);

      // Invalid format, exclude from output
      if (!col.has("name") || !col.has("direction"))
        continue;

      final String key = col.getString("name");
      final String dir = col.getString("direction");

      // Invalid column name, exclude from output
      if (!valid.containsKey(key))
        continue;

      // Sort direction is not valid, exclude from output
      if (!SortDirection.isValidDirection(dir))
        continue;

      out.put(key, SortDirection.valueOf(dir).isAscending());
    }

    return out;
  }

  public static Map<String, AttributeField> parseSummaryMap(JSONObject obj,
      Map<String, AttributeField> fields) {
    final Map<String, AttributeField> out = new LinkedHashMap<>();

    if (!hasColumnSelection(obj))
      return out;

    final JSONArray arr = obj.getJSONArray("columnSelection");
    for (int i = 0; i < arr.length(); i++) {
      final String field = arr.getString(i);
      if (!fields.containsKey(field))
        continue;

      out.put(field, fields.get(field));
    }

    return out;
  }
}
