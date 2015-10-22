package org.gusdb.wdk.service.formatter.param;

import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.AbstractEnumParam;
import org.json.JSONException;
import org.json.JSONObject;

public class EnumParamFormatter extends AbstractEnumParamFormatter implements VocabProvider {

  EnumParamFormatter(AbstractEnumParam param) {
    super(param);
  }

  @Override
  public JSONObject getJson(Map<String, String> dependedParamValues)
      throws JSONException, WdkModelException {
    JSONObject pJson = super.getJson();
    // TODO: add vocabulary information
    return pJson;
  }
}
