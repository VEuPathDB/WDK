package org.gusdb.wdk.service.formatter;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.AbstractEnumParam;
import org.gusdb.wdk.model.query.param.Param;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class EnumParamFormatter {

	public static JSONObject getParamJson(AbstractEnumParam param) throws JSONException, WdkModelException {

		JSONObject pJson = ParamFormatter.getParamJson(param);
		pJson.put("countOnlyLeaves", param.getCountOnlyLeaves());
		pJson.put("maxSelectedCount", param.getMaxSelectedCount());
		pJson.put("minSelectedCount", param.getMinSelectedCount());
		pJson.put("multiPick", param.getMultiPick());
		pJson.put("displayType", param.getDisplayType());
		pJson.put("isDependentParam", param.isDependentParam());

		if (param.isDependentParam()) {
			JSONArray dependedParamNames = new JSONArray();
			for (Param dp : param.getDependedParams()) {
				dependedParamNames.put(dp.getName());
			}
			pJson.put("dependedParamNames", dependedParamNames);
		}
		// vocab treeMap currentValues displayMap

		return pJson;
	}

}
