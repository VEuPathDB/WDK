package org.gusdb.wdk.controller.action;

import java.io.InputStream;
import java.util.Map;

import org.gusdb.fgputil.IoUtil;
import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamDef;
import org.gusdb.wdk.controller.actionutil.ParamDef.DataType;
import org.gusdb.wdk.controller.actionutil.ParamDef.Required;
import org.gusdb.wdk.controller.actionutil.ParamDefMapBuilder;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.controller.actionutil.ResponseType;
import org.gusdb.wdk.controller.actionutil.WdkAction;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.StepFactory;
import org.gusdb.wdk.model.user.Strategy;
import org.json.JSONException;
import org.json.JSONObject;

public class ProcessPublicStratsStatusAction extends WdkAction {

  private static final String STRAT_ID_PARAM_KEY = "stratId";
  private static final String IS_PUBLIC_PARAM_KEY = "isPublic";

  private static final Map<String, ParamDef> PARAMS = new ParamDefMapBuilder()
    .addParam(STRAT_ID_PARAM_KEY, new ParamDef(Required.REQUIRED, DataType.INTEGER))
    .addParam(IS_PUBLIC_PARAM_KEY, new ParamDef(Required.REQUIRED, DataType.BOOLEAN))
    .toMap();
  
  @Override protected boolean shouldValidateParams() { return true; }
  @Override protected Map<String, ParamDef> getParamDefs() { return PARAMS; }

  @Override
  protected ActionResult handleRequest(ParamGroup params) throws Exception {
    int stratId = params.getIntValue(STRAT_ID_PARAM_KEY);
    boolean isPublic = params.getBooleanValue(IS_PUBLIC_PARAM_KEY);
    StepFactory factory = getWdkModel().getModel().getStepFactory();
    Strategy strat = factory.getStrategyById(stratId);
    if (strat.getUser().getUserId() != getCurrentUser().getUserId()) {
      // only allow owner of strategy to update is_public status
      throw new WdkUserException("Permission Denied. " +
          "You must own this strategy to make it public.");
    }
    factory.setStrategyPublicStatus(stratId, isPublic);
    return new ActionResult(ResponseType.json).setStream(getJsonSuccessResultStream());
  }
  
  private InputStream getJsonSuccessResultStream() throws WdkModelException {
    try {
      JSONObject json = new JSONObject();
      json.put("success", true);
      return IoUtil.getStreamFromString(json.toString());
    }
    catch (JSONException e) {
      // this should never happen
      throw new WdkModelException("Failed to create JSON object.");
    }
  }

}
