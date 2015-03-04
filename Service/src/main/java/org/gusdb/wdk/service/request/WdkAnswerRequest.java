package org.gusdb.wdk.service.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.beans.FilterValue;
import org.gusdb.wdk.beans.ParamValue;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerFilterInstance;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.user.User;
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
   *   “filters”: [ {
   *     “name”: String, value: Any
   *   } ]
   * }
   * 
   * @param json
   * @param model
   * @return
   * @throws RequestMisformatException
   */
  public static WdkAnswerRequest createFromJson(User user, JSONObject json, WdkModelBean model) throws RequestMisformatException {
    try {
      // get question name, validate, and create instance with valid Question
      String questionFullName = json.getString("questionName");
      model.validateQuestionFullName(questionFullName);
      Question question = model.getModel().getQuestion(questionFullName);
      WdkAnswerRequest request = new WdkAnswerRequest(question);
      request.setParamValues(parseParamValues(json.getJSONArray("params"), question, model));
      request.setFilterValues(parseFilterValues(json.getJSONArray("filters"), question, model));
      return request;
    }
    catch (JSONException | WdkUserException e) {
      throw new RequestMisformatException("Required value is missing or incorrect type", e);
    }
    catch (WdkModelException e) {
      throw new WdkRuntimeException("Error creating request from JSON", e);
    }
  }

  private static List<FilterValue> parseFilterValues(JSONArray jsonArray,
      Question question, WdkModelBean model) throws WdkUserException {
    // parse filter values and validate
    Map<String, Object> inputValueMap = getContextValues(jsonArray);
    List<FilterValue> filterValues = new ArrayList<>();
    for (String filterName : inputValueMap.keySet()) {
      AnswerFilterInstance filter = question.getRecordClass().getFilter(filterName);
      if (filter == null) {
        throw new WdkUserException("Filter name " + filterName +
            " does not exist for record class " + question.getRecordClass().getFullName());
      }
      filterValues.add(new FilterValue(filter, inputValueMap.get(filterName)));
    }
    return filterValues;
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

  private final Question _question;
  private Map<String, ParamValue> _params = new HashMap<>();
  private List<FilterValue> _filters = new ArrayList<>();

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

  public List<FilterValue> getFilterValues() {
    return _filters;
  }
  private void setFilterValues(List<FilterValue> filters) {
    _filters = filters;
  }
}
