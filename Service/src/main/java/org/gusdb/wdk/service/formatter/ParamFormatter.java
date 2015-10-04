package org.gusdb.wdk.service.formatter;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.Param;
import org.json.JSONException;
import org.json.JSONObject;

public class ParamFormatter {
	
	public static JSONObject getParamJson(Param param)
		      throws JSONException, WdkModelException {
			  
		    JSONObject pJson = new JSONObject();
		    pJson.put("name", param.getName());
		    pJson.put("displayName", param.getName());
		    pJson.put("prompt", param.getPrompt());
		    pJson.put("help", param.getHelp());
		    pJson.put("defaultValue", param.getDefault());
		    pJson.put("type", param.getClass().getSimpleName());
		    pJson.put("isVisible", param.isVisible());
		    pJson.put("group", param.getGroup());
		    pJson.put("isReadOnly", param.isReadonly());
		    pJson.put("fullName", param.getFullName());
	        pJson.put("id", param.getId());
		    return pJson;
	}
}
