package org.gusdb.wdk.service.formatter;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.json.JSONException;
import org.json.JSONObject;

public class AnswerParamFormatter  {

	public static JSONObject getParamJson(AnswerParam param)
		      throws JSONException, WdkModelException {
			  
		    JSONObject pJson = ParamFormatter.getParamJson(param);
		    return pJson;
	}
}
