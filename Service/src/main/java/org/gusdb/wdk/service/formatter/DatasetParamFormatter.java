package org.gusdb.wdk.service.formatter;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.dataset.DatasetParser;
import org.gusdb.wdk.model.query.param.DatasetParam;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DatasetParamFormatter {
	public static JSONObject getParamJson(DatasetParam param)
		      throws JSONException, WdkModelException {
			  
		    JSONObject pJson = ParamFormatter.getParamJson(param);
		    JSONArray parsersJson = new JSONArray();
		    pJson.put("parsers", parsersJson);
		    for (DatasetParser parser : param.getParsers()) {
		    	JSONObject parserJson = new JSONObject();
		    	parsersJson.put(parserJson);
		    	parserJson.put("name", parser.getName());
		    	parserJson.put("display", parser.getDisplay());
		    	parserJson.put("description", parser.getDescription());
		    }
		    return pJson;
	}

}
