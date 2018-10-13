package org.gusdb.wdk.service.formatter.param;

import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dataset.DatasetParser;
import org.gusdb.wdk.model.query.param.DatasetParam;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DatasetParamFormatter extends ParamFormatter<DatasetParam> {

  DatasetParamFormatter(DatasetParam param) {
    super(param);
  }

  @Override
  public JSONObject getJson() throws JSONException, WdkModelException, WdkUserException {

    JSONObject pJson = super.getJson();
    JSONArray parsersJson = new JSONArray();
    pJson.put(JsonKeys.PARSERS, parsersJson);
    for (DatasetParser parser : _param.getParsers()) {
      JSONObject parserJson = new JSONObject();
      parsersJson.put(parserJson);
      parserJson.put(JsonKeys.NAME, parser.getName());
      parserJson.put(JsonKeys.DISPLAY_NAME, parser.getDisplay());
      parserJson.put(JsonKeys.DESCRIPTION, parser.getDescription());
    }
    return pJson;
  }

}
