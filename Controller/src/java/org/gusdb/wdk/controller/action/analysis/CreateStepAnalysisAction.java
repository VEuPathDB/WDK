package org.gusdb.wdk.controller.action.analysis;

import java.util.Map;

import org.gusdb.fgputil.IoUtil;
import org.gusdb.fgputil.MapBuilder;
import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamDef;
import org.gusdb.wdk.controller.actionutil.ParamDef.DataType;
import org.gusdb.wdk.controller.actionutil.ParamDef.Required;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.controller.actionutil.ResponseType;
import org.gusdb.wdk.controller.actionutil.WdkAction;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.analysis.IllegalStepException;
import org.gusdb.wdk.model.user.analysis.StepAnalysisContext;
import org.gusdb.wdk.model.user.analysis.StepAnalysisFactory;
import org.json.JSONException;
import org.json.JSONObject;

public class CreateStepAnalysisAction extends WdkAction {

  private static final String ANALYSIS_NAME_KEY = "analysisName";
  private static final String STRATEGY_ID_KEY = "strategyId";
  private static final String STEP_ID_KEY = "stepId";

  private static enum JsonKey { status, message }
  
  private static final Map<String, ParamDef> PARAMS = new MapBuilder<String,ParamDef>()
      .put(ANALYSIS_NAME_KEY, new ParamDef(Required.REQUIRED))
      .put(STRATEGY_ID_KEY, new ParamDef(Required.REQUIRED, DataType.INTEGER))
      .put(STEP_ID_KEY, new ParamDef(Required.REQUIRED, DataType.INTEGER))
      .toMap();

  @Override protected boolean shouldValidateParams() { return true; }
  @Override protected Map<String, ParamDef> getParamDefs() { return PARAMS; }
    
  @Override
  protected ActionResult handleRequest(ParamGroup params) throws Exception {
    
    StepAnalysisFactory analysisMgr = getWdkModel().getModel().getStepAnalysisFactory();
    
    StepAnalysisContext context = StepAnalysisContext.createNewContext(
        getCurrentUser(), params.getValue(ANALYSIS_NAME_KEY),
        params.getIntValue(STRATEGY_ID_KEY), params.getIntValue(STEP_ID_KEY));
    
    try {
      context = analysisMgr.createAnalysis(context);
      return AbstractStepAnalysisIdAction.getStepAnalysisJsonResult(context);
    }
    catch (IllegalStepException ise) {
      return getInvalidStepResult(ise.getMessage());
    }
  }
  
  private ActionResult getInvalidStepResult(String userMessage) throws WdkModelException {
    try {
      // this analysis cannot be applied to this step
      JSONObject json = new JSONObject();
      json.put(JsonKey.status.name(), "validation");
      json.put(JsonKey.message.name(), userMessage);
      return new ActionResult(ResponseType.json)
          .setStream(IoUtil.getStreamFromString(json.toString()));
    }
    catch (JSONException e) {
      throw new WdkModelException("Unable to generate JSON response object.", e);
    }
  }
}
