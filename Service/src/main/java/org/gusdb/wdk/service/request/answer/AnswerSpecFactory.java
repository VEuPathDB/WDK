package org.gusdb.wdk.service.request.answer;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerFilterInstance;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.answer.spec.ParamValue;
import org.gusdb.wdk.model.filter.Filter;
import org.gusdb.wdk.model.filter.FilterOption;
import org.gusdb.wdk.model.filter.FilterOptionList;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.formatter.Keys;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AnswerSpecFactory {

  private static final Logger LOG = Logger.getLogger(AnswerSpec.class);

  public static AnswerSpec createFromQuestion(Question question) {
    return new AnswerSpec(question);
  }

  public static AnswerSpec createFromStep(Step step) throws WdkModelException {
    Question question = step.getQuestion();
    AnswerSpec answerSpec = new AnswerSpec(question);
    answerSpec.setParamValues(toParamValueMap(question, step.getParamValues()));
    answerSpec.setLegacyFilter(step.getFilter());
    answerSpec.setFilterValues(step.getFilterOptions());
    answerSpec.setViewFilterValues(step.getViewFilterOptions());
    answerSpec.setWeight(step.getAssignedWeight());
    return answerSpec;
  }

  private static Map<String, ParamValue> toParamValueMap(Question question, Map<String, String> paramValues) {
    Map<String, Param> questionParams = question.getParamMap();
    Map<String, ParamValue> paramValueMap = new HashMap<>();
    for (String key : paramValues.keySet()) {
      paramValueMap.put(key, new ParamValue(questionParams.get(key), paramValues.get(key)));
    }
    return paramValueMap;
  }

  /**
   * Creates an AnswerRequest object using the passed JSON.  "questionName" is
   * the only required property; however "params" must be specified as required
   * by the question passed.  Legacy and modern filters are optional; omission
   * means no filters will be applied.  Weight is optional and defaults to 0.
   * 
   * Input Format:
   * {
   *   "questionName" : String,
   *   "parameters": Object (map from paramName -> paramValue),
   *   "legacyFilterName": (optional) String,
   *   "filters": (optional) [ {
   *     "name": String, value: Any
   *   } ],
   *   "viewFilters": (optional) [ {
   *     "name": String, value: Any
   *   } ],
   *   "wdk_weight": (optional) Integer
   * }
   * 
   * @param json JSON representation of an answer request
   * @param modelBean WDK model bean
   * @return answer request object constructed
   * @throws RequestMisformatException if JSON is malformed
   */
  public static AnswerSpec createFromJson(JSONObject json, WdkModelBean modelBean, User user, boolean expectIncompleteSpec) throws DataValidationException, RequestMisformatException {
    try {
      // get question name, validate, and create instance with valid Question
      String questionName = json.getString(Keys.QUESTION_NAME);
      modelBean.validateQuestionFullName(questionName);
      WdkModel model = modelBean.getModel();
      Question question = model.getQuestion(questionName);
      AnswerSpec request = new AnswerSpec(question);
      // params are required (empty array if no params)
      request.setParamValues(parseParamValues(json.getJSONObject(Keys.PARAMETERS), question, user, expectIncompleteSpec));
      // all filter fields are optional
      if (json.has(Keys.LEGACY_FILTER_NAME)) {
        request.setLegacyFilter(getLegacyFilter(json.getString(Keys.LEGACY_FILTER_NAME), question));
      }
      request.setFilterValues(json.has(Keys.FILTERS) ?
          parseFilterValues(json.getJSONArray(Keys.FILTERS), question, model, false) :
            new FilterOptionList(model, questionName));
      request.setViewFilterValues(json.has(Keys.VIEW_FILTERS) ?
          parseFilterValues(json.getJSONArray(Keys.VIEW_FILTERS), question, model, true) :
            new FilterOptionList(model, questionName));
      if (json.has(Keys.WDK_WEIGHT)) {
        request.setWeight(json.getInt(Keys.WDK_WEIGHT));
      }
      return request;
    }
    catch (JSONException e) {
      throw new RequestMisformatException("Required value is missing or incorrect type", e);
    }
    catch (WdkUserException e) {
      throw new DataValidationException(e);
    }
    catch (WdkModelException e) {
      throw new WdkRuntimeException("Error querying model during answer request parsing", e);
    }
  }

  private static AnswerFilterInstance getLegacyFilter(String filterName, Question question)
      throws RequestMisformatException {
    AnswerFilterInstance filter = question.getRecordClass().getFilterInstance(filterName);
    if (filter == null) {
      throw new RequestMisformatException("Specified legacy filter name does not match filter for this record class.");
    }
    return filter;
  }

  private static FilterOptionList parseFilterValues(JSONArray jsonArray,
      Question question, WdkModel model, boolean isViewFilters) throws WdkUserException {
    // parse filter values and validate
    String questionName = question.getFullName();
    Map<String, JSONObject> inputValueMap = getContextValues(jsonArray);
    FilterOptionList filterList = new FilterOptionList(model, questionName);
    for (String filterName : inputValueMap.keySet()) {
      try {
        Filter filter = question.getFilter(filterName);
        if (filter.getIsViewOnly() != isViewFilters) {
          throw new WdkUserException("[" + filterName + "] Cannot use a regular filter as a view filter or vice-versa.");
        }
        filterList.addFilterOption(new FilterOption(model, questionName, filterName, inputValueMap.get(filterName)));
      }
      catch (WdkModelException e) {
        
      }
    }
    return filterList;
  }

  private static Map<String, ParamValue> parseParamValues(JSONObject paramsJson,
      Question question, User user, boolean expectIncompleteSpec) throws WdkUserException, WdkModelException {
    // parse param values and validate
    Map<String, Param> expectedParams = question.getParamMap();
    Map<String, String> contextValues = getContextValues(paramsJson);

    // loop through expected params and build valid list of values from request
    Map<String, ParamValue> paramValues = new HashMap<>();
    for (Param expectedParam : expectedParams.values()) {
      String paramName = expectedParam.getName();
      String stableValue = null;
      if (!contextValues.containsKey(paramName)) {
        if (!expectedParam.isAllowEmpty()) {
          throw new WdkUserException("Required parameter '" + paramName + "' is missing.");
        }
        else {
          // FIXME RRD: leaving this here for now, but have reason to believe emptyValue is an internal value
          //      to be inserted into SQL at the last moment, NOT a stable value to be stored in the DB.
          //      Not sure what that means for this code - probably that we should leave this param missing
          //      but not throw an error.  However, it hoses our validation strategy on the branch since
          //      we can only validate stable values- how can we validate the emptyValue?
          stableValue = expectedParam.getEmptyValue();
        }
      }
      else {
        stableValue = contextValues.get(paramName);
      }
      // stableValue is now either a string or Java null
      if (expectedParam instanceof AnswerParam && expectIncompleteSpec && stableValue != null) {
        // null actually expected as parameter value; error if not the case
        throw new WdkUserException("Unattached steps' answer params must have null values.");
      }
      paramValues.put(paramName, new ParamValue(expectedParam,
          expectedParam.getParamHandler().validateStableValueSyntax(user, stableValue)));
    }
    return paramValues;
  }

  // TODO: Would like to return Map<String,JsonValue> here but need to upgrade to javax.json.
  //       For now, return Map<String,String> where value might be null if incoming value is JSONObject.NULL
  private static Map<String, String> getContextValues(
      JSONObject contextObject) throws JSONException, WdkUserException {
    Map<String, String> contextValues = new HashMap<>();
    for (String name : JsonUtil.getKeys(contextObject)) {
      Object value = contextObject.get(name);
      if (JSONObject.NULL.equals(value)) {
        contextValues.put(name, null);
      }
      else if (value instanceof String) {
        contextValues.put(name, (String)value);
      }
      else {
        throw new WdkUserException("Parameter '" + name + "' is not a string or null.");
      }
      LOG.debug("Added request parameter '" + name + "', value = " + contextValues.get(name));
    }
    return contextValues;
  }

  private static Map<String, JSONObject> getContextValues(
      JSONArray contextArray) throws JSONException {
    Map<String, JSONObject> contextValues = new HashMap<>();
    if (contextArray == null) return contextValues;
    for (int i = 0; i < contextArray.length(); i++) {
      JSONObject obj = contextArray.getJSONObject(i);
      String name = obj.getString(Keys.NAME);
      contextValues.put(name, obj.getJSONObject(Keys.VALUE));
      LOG.info("Added request parameter '" + name +
          "', value = " + contextValues.get(name).toString());
    }
    return contextValues;
  }

}
