package org.gusdb.wdk.service.formatter.param;

import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.jspwrap.EnumParamVocabInstance;
import org.gusdb.wdk.model.query.param.AbstractEnumParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.user.User;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class AbstractEnumParamFormatter extends ParamFormatter<AbstractEnumParam> {

  AbstractEnumParamFormatter(AbstractEnumParam param) {
    super(param);
  }

  @Override
  public JSONObject getJson() throws JSONException, WdkModelException {

    JSONObject pJson = super.getJson();
    pJson.put("countOnlyLeaves", _param.getCountOnlyLeaves());
    pJson.put("maxSelectedCount", _param.getMaxSelectedCount());
    pJson.put("minSelectedCount", _param.getMinSelectedCount());
    pJson.put("multiPick", _param.getMultiPick());
    pJson.put("displayType", _param.getDisplayType());
    pJson.put("isDependentParam", _param.isDependentParam());

    if (_param.isDependentParam()) {
      JSONArray dependedParamNames = new JSONArray();
      for (Param dp : _param.getDependedParams()) {
        dependedParamNames.put(dp.getName());
      }
      pJson.put("dependedParamNames", dependedParamNames);
    }
    
    return pJson;
  }
  
  protected JSONArray getVocabJson(User user, Map<String, String> dependedParamValues) throws WdkModelException {

    EnumParamVocabInstance vocabInstance = getParam().getVocabInstance(user, dependedParamValues);
    List<List<String>> vocabRows = vocabInstance.getFullVocab();
    JSONArray jsonRows = new JSONArray();
    for (List<String> row : vocabRows) {
      if (row.size() != 3) throw new WdkModelException("Enum vocab includes a row that does not contain 3 columns");
      JSONArray jsonRow = new JSONArray();
      jsonRow.put(row.get(0));
      jsonRow.put(row.get(1));
      jsonRow.put(row.get(2));
      jsonRows.put(jsonRow);
    }
    return jsonRows;
  }

}
