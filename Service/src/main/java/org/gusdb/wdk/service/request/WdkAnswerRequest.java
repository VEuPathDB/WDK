package org.gusdb.wdk.service.request;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.beans.ParamValue;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerFilterInstance;
import org.gusdb.wdk.model.filter.Filter;
import org.gusdb.wdk.model.filter.FilterOption;
import org.gusdb.wdk.model.filter.FilterOptionList;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.question.Question;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WdkAnswerRequest {

  private static final Logger LOG = Logger.getLogger(WdkAnswerRequest.class);
  
  /**
   * Input Format:
   * {
   *   “questionName”: String,
   *   “params”: [ {
   *     “name”: String, “value”: Any
   *   } ],
 *     "legacyFilterName": String,
   *   “filters”: [ {
   *     “name”: String, value: Any
   *   } ],
   *   “viewFilters”: [ {
   *     “name”: String, value: Any
   *   } ]
   * }
   * 
   * @param json
   * @param model
   * @return
   * @throws RequestMisformatException
   */
  public static WdkAnswerRequest createFromJson(JSONObject json, WdkModelBean model) throws RequestMisformatException {
    try {
      // get question name, validate, and create instance with valid Question
      String questionFullName = json.getString("questionName");
      model.validateQuestionFullName(questionFullName);
      Question question = model.getModel().getQuestion(questionFullName);
      WdkAnswerRequest request = new WdkAnswerRequest(question);
      // params are required (empty array if no params)
      request.setParamValues(parseParamValues(json.getJSONArray("params"), question, model));
      // all filter fields are optional
      if (json.has("legacyFilterName")) {
        request.setLegacyFilter(getLegacyFilter(json.getString("legacyFilterName"), question));
      }
      request.setFilterValues(json.has("filters") ?
          parseFilterValues(json.getJSONArray("filters"), question, model, false) :
            new FilterOptionList(question));
      request.setViewFilterValues(json.has("viewFilters") ?
          parseFilterValues(json.getJSONArray("viewFilters"), question, model, true) :
            new FilterOptionList(question));
      return request;
    }
    catch (JSONException | WdkUserException e) {
      throw new RequestMisformatException("Required value is missing or incorrect type", e);
    }
    catch (WdkModelException e) {
      throw new WdkRuntimeException("Error creating request from JSON", e);
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
      Question question, WdkModelBean model, boolean isViewFilters) throws WdkUserException {
    // parse filter values and validate
    Map<String, JSONObject> inputValueMap = getContextJsonValues(jsonArray);
    FilterOptionList filterList = new FilterOptionList(question);
    for (String filterName : inputValueMap.keySet()) {
      try {
        Filter filter = question.getFilter(filterName);
        if (filter.getIsViewOnly() != isViewFilters) {
          throw new WdkUserException("[" + filterName + "] Cannot use a regular filter as a view filter or vice-versa.");
        }
        filterList.addFilterOption(new FilterOption(question, filter, inputValueMap.get(filterName)));
      }
      catch (WdkModelException e) {
        
      }
    }
    return filterList;
  }

  private static Map<String, ParamValue> parseParamValues(JSONArray paramsJson,
      Question question, WdkModelBean model) throws WdkUserException {
    // parse param values and validate
    Map<String, Param> expectedParams = question.getParamMap();
    Map<String, Object> contextValues = getContextValues(paramsJson);

    // loop through expected params and build valid list of values from request
    Map<String, ParamValue> paramValues = new HashMap<>();
    for (Param expectedParam : expectedParams.values()) {
      String paramName = expectedParam.getName();
      ParamValue value;
      if (!contextValues.containsKey(paramName)) {
        if (!expectedParam.isAllowEmpty()) {
          throw new WdkUserException("Required parameter '" + paramName + "' is missing.");
        }
        else {
          value = new ParamValue(expectedParam, expectedParam.getEmptyValue());
        }
      }
      else {
        value = new ParamValue(expectedParam, contextValues.get(paramName));
      }
      paramValues.put(paramName, value);

    }
    return paramValues;
  }

  // TODO: would like to get rid of this and only use getJsonContextValues
  private static Map<String, Object> getContextValues(
      JSONArray namedObjectArrayJson) throws JSONException {
    Map<String, Object> contextValues = new HashMap<>();
    for (int i = 0; i < namedObjectArrayJson.length(); i++) {
      JSONObject obj = namedObjectArrayJson.getJSONObject(i);
      String name = obj.getString("name");
      contextValues.put(name, obj.get("value"));
      LOG.info("Added request parameter '" + name +
          "', value = " + contextValues.get(name).toString());
    }
    return contextValues;
  }

  private static Map<String, JSONObject> getContextJsonValues(
      JSONArray namedObjectArrayJson) throws JSONException {
    Map<String, JSONObject> contextValues = new HashMap<>();
    for (int i = 0; i < namedObjectArrayJson.length(); i++) {
      JSONObject obj = namedObjectArrayJson.getJSONObject(i);
      String name = obj.getString("name");
      contextValues.put(name, obj.getJSONObject("value"));
      LOG.info("Added request parameter '" + name +
          "', value = " + contextValues.get(name).toString());
    }
    return contextValues;
  }

  private final Question _question;
  private Map<String, ParamValue> _params = new HashMap<>();
  private AnswerFilterInstance _legacyFilter;
  private FilterOptionList _filters;
  private FilterOptionList _viewFilters;

  private WdkAnswerRequest(Question question) {
    _question = question;
  }

  public Question getQuestion() {
    return _question;
  }

  public Map<String, ParamValue> getParamValues() {
    return _params;
  }
  private void setParamValues(Map<String, ParamValue> params) {
    _params = params;
  }

  public AnswerFilterInstance getLegacyFilter() {
    return _legacyFilter;
  }
  private void setLegacyFilter(AnswerFilterInstance legacyFilter) {
    _legacyFilter = legacyFilter;
  }

  public FilterOptionList getFilterValues() {
    return _filters;
  }
  private void setFilterValues(FilterOptionList filters) {
    _filters = filters;
  }

  public FilterOptionList getViewFilterValues() {
    return _viewFilters;
  }
  private void setViewFilterValues(FilterOptionList viewFilters) {
    _viewFilters = viewFilters;
  }
}
