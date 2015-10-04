package org.gusdb.wdk.service.formatter;

import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.AbstractEnumParam;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.DatasetParam;
import org.gusdb.wdk.model.query.param.FilterParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.StringParam;
import org.gusdb.wdk.model.query.param.TimestampParam;
import org.gusdb.wdk.model.question.Question;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class QuestionFormatter {

  public static JSONArray getQuestionsJson(List<Question> questions, boolean expandQuestions, boolean expandParams, Map<String, String>dependerParamValues)
      throws JSONException, WdkModelException {
    JSONArray json = new JSONArray();
    for (Question q : questions) {
      if (expandQuestions) {
        json.put(getQuestionJson(q, expandParams, dependerParamValues));
      }
      else {
        json.put(q.getFullName());
      }
    }
    return json;
  }

  public static JSONObject getQuestionJson(Question q, boolean expandParams, Map<String, String>dependerParamValues)
      throws JSONException, WdkModelException {
    JSONObject qJson = new JSONObject();
    qJson.put("name", q.getFullName());
    qJson.put("displayName", q.getDisplayName());
    qJson.put("class", q.getRecordClass().getFullName());
    qJson.put("params", getParamsJson(q, expandParams, dependerParamValues));
    return qJson;
  }

  public static JSONArray getParamsJson(Question q, boolean expandParams, Map<String, String>dependerParamValues)
      throws JSONException, WdkModelException {
    JSONArray params = new JSONArray();
    for (Param param : q.getParams()) {
      if (expandParams) {
        boolean includeVocabListing = param instanceof AbstractEnumParam && !(param instanceof FilterParam ||
            ((AbstractEnumParam) param).getDisplayType().equals("typeAhead"));
        params.put(getParamJson(param, includeVocabListing, dependerParamValues));
      }
      else {
        params.put(param.getFullName());
      }
    }
    return params;
  }

  public static JSONObject getParamJson(Param param, boolean includeVocabListing, Map<String, String>dependerParamValues)
      throws JSONException, WdkModelException {
	  
	  JSONObject pJson = null;
      if (param instanceof FilterParam) {
          pJson = FilterParamFormatter.getParamJson((FilterParam)param, includeVocabListing, dependerParamValues);
        }else if (param instanceof AbstractEnumParam) {
        	pJson = EnumParamFormatter.getParamJson((AbstractEnumParam)param, includeVocabListing, dependerParamValues);
        } else if (param instanceof AnswerParam) {
        	pJson = AnswerParamFormatter.getParamJson((AnswerParam)param);
        } else if (param instanceof DatasetParam) {
        	pJson = DatasetParamFormatter.getParamJson((DatasetParam)param);
        } else if (param instanceof TimestampParam) {
        	pJson = ParamFormatter.getParamJson(param);
        } else if (param instanceof StringParam) {
        	pJson = StringParamFormatter.getParamJson((StringParam)param);
        } else {
            throw new WdkModelException("Unknown param type: " + param.getClass().getCanonicalName());
        }
    return pJson;
  }
}
