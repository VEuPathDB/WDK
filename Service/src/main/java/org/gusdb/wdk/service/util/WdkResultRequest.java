package org.gusdb.wdk.service.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.FilterBean;
import org.gusdb.wdk.model.jspwrap.ParamBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WdkResultRequest {

  /*
{
  "questionName": "GenesByExonCount",
  "params": [
    { "name": "min", "value": 2 },
    { "name": "max", "value": 20 }
  ]
}
    
    
  }
  
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
  public static WdkResultRequest createFromJson(JSONObject json, WdkModelBean model) throws RequestMisformatException {
    try {
      // get question name, validate, and create instance with valid Question
      String questionFullName = json.getString("questionName");
      model.validateQuestionFullName(questionFullName);
      QuestionBean question = model.getQuestion(questionFullName);
      WdkResultRequest request = new WdkResultRequest(question);

      // parse param values and validate
      JSONArray paramsJson = json.getJSONArray("params");
      for (int i = 0; i < paramsJson.length(); i++) {
        JSONObject paramJson = paramsJson.getJSONObject(i);
        String paramName = paramJson.getString("name");
        Object paramValue = paramJson.get("value");
        Map<String, ParamBean<?>> expectedParams = question.getParamsMap();
        
      }
      
      // parse filter values and validate
      
      return request;
    }
    catch (JSONException | WdkUserException e) {
      throw new RequestMisformatException("Required value is missing or incorrect type", e);
    }
    catch (WdkModelException e) {
      throw new WdkRuntimeException("Error creating request from JSON", e);
    }
  }

  private final QuestionBean _question;
  private Map<String, ParamBean<?>> _params = new HashMap<>();
  private List<FilterBean> _filters = new ArrayList<>();

  private WdkResultRequest(QuestionBean question) {
    _question = question;
  }

  public QuestionBean getQuestion() {
    return _question;
  }

  public Map<String, ParamBean<?>> getParamValues() {
    return _params;
  }

  public List<FilterBean> getFilterValues() {
    return _filters;
  }
}
