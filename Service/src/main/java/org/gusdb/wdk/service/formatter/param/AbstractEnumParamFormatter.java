package org.gusdb.wdk.service.formatter.param;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.AbstractEnumParam;
import org.gusdb.wdk.model.query.param.Param;
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
    // vocab treeMap currentValues displayMap

    return pJson;
  }

}
