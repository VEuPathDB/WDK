package org.gusdb.wdk.service.formatter;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.StringParam;
import org.json.JSONException;
import org.json.JSONObject;

public class StringParamFormatter  {
	public static JSONObject getParamJson(StringParam param)
		      throws JSONException, WdkModelException {
			  
		    JSONObject pJson = ParamFormatter.getParamJson(param);
		    pJson.put("length", param.getLength());
		    return pJson;
	}
}
