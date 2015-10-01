package org.gusdb.wdk.service.formatter;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.FilterParam;
import org.json.JSONException;
import org.json.JSONObject;

public class FilterParamFormatter {
	
	public static JSONObject getParamJson(FilterParam param)
		      throws JSONException, WdkModelException {
			  
		    JSONObject pJson = EnumParamFormatter.getParamJson(param);
		    return pJson;
	}

}
