package org.gusdb.wdk.service.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.beans.FilterValue;
import org.gusdb.wdk.beans.ParamValue;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.user.User;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WdkResultRequest {

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
  public static WdkResultRequest createFromJson(User user, JSONObject json, WdkModelBean model) throws RequestMisformatException {
    try {
      // get question name, validate, and create instance with valid Question
      String questionFullName = json.getString("questionName");
      model.validateQuestionFullName(questionFullName);
      Question question = model.getModel().getQuestion(questionFullName);
      WdkResultRequest request = new WdkResultRequest(question);

      // parse param values and validate
      JSONArray paramsJson = json.getJSONArray("params");
      Map<String, Param> expectedParams = question.getParamMap();
      Map<String, Object> contextValues = getContextValues(paramsJson);

      // loop through expected params and build valid list of values from request
      Map<String, ParamValue> validatedValues = new HashMap<>();
      for (Param expectedParam : expectedParams.values()) {
        String paramName = expectedParam.getName();
        ParamValue value;
        if (!contextValues.containsKey(expectedParam)) {
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
        validatedValues.put(paramName, value);

      }
      request.setParamValues(validatedValues);
      
      // parse filter values and validate
      // TODO: parse filters; for now filters are unsupported
      request.setFilterValues(Collections.<FilterValue>emptyList());
      
      return request;
    }
    catch (JSONException | WdkUserException e) {
      throw new RequestMisformatException("Required value is missing or incorrect type", e);
    }
    catch (WdkModelException e) {
      throw new WdkRuntimeException("Error creating request from JSON", e);
    }
  }

  private static Map<String, Object> getContextValues(JSONArray paramsJson) {
    Map<String, Object> contextValues = new HashMap<>();
    for (int i = 0; i < paramsJson.length(); i++) {
      JSONObject param = paramsJson.getJSONObject(i);
      contextValues.put(param.getString("name"), param.get("value"));
    }
    return contextValues;
  }

  private final Question _question;
  private Map<String, ParamValue> _params = new HashMap<>();
  private List<FilterValue> _filters = new ArrayList<>();

  private WdkResultRequest(Question question) {
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
