package org.gusdb.wdk.service.formatter.param;

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
    pJson.put("parsers", parsersJson);
    for (DatasetParser parser : _param.getParsers()) {
      JSONObject parserJson = new JSONObject();
      parsersJson.put(parserJson);
      parserJson.put("name", parser.getName());
      parserJson.put("display", parser.getDisplay());
      parserJson.put("description", parser.getDescription());
    }
    return pJson;
  }

}
