package org.gusdb.wdk.service.formatter;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.TimestampParam;
import org.json.JSONException;
import org.json.JSONObject;

public class TimestampParamFormatter  {
	public static JSONObject getParamJson(TimestampParam param)
		      throws JSONException, WdkModelException {
			  
		    JSONObject pJson = ParamFormatter.getParamJson(param);
		    return pJson;
	}

}
