package org.gusdb.wdk.service.formatter.param;

import org.gusdb.fgputil.validation.ValidObjectFactory.DisplayablyValid;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.dataset.DatasetParser;
import org.gusdb.wdk.model.query.param.DatasetParam;
import org.gusdb.wdk.model.query.spec.ParameterContainerInstanceSpec;
import org.json.JSONArray;
import org.json.JSONObject;

public class DatasetParamFormatter extends ParamFormatter<DatasetParam> {

  DatasetParamFormatter(DatasetParam param) {
    super(param);
  }

  @Override
  public <S extends ParameterContainerInstanceSpec<S>> JSONObject getJson(DisplayablyValid<S> spec) throws WdkModelException {
    JSONObject pJson = getBaseJson(spec);
    JSONArray parsersJson = new JSONArray();
    pJson.put(JsonKeys.DEFAULT_ID_LIST, _param.getXmlDefault());
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
  
  @Override
  public String getParamType() {
    return JsonKeys.DATASET_PARAM_TYPE;
  }

}
