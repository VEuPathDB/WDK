package org.gusdb.wdk.service.formatter.param;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dataset.DatasetParser;
import org.gusdb.wdk.model.query.param.DatasetParam;
import org.gusdb.wdk.service.formatter.Keys;
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
    pJson.put(Keys.PARSERS, parsersJson);
    for (DatasetParser parser : _param.getParsers()) {
      JSONObject parserJson = new JSONObject();
      parsersJson.put(parserJson);
      parserJson.put(Keys.NAME, parser.getName());
      parserJson.put(Keys.DISPLAY_NAME, parser.getDisplay());
      parserJson.put(Keys.DESCRIPTION, parser.getDescription());
    }
    return pJson;
  }

}
