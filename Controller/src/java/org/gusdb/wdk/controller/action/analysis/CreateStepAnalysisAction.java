package org.gusdb.wdk.controller.action.analysis;

import java.util.Map;

import org.gusdb.fgputil.MapBuilder;
import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamDef;
import org.gusdb.wdk.controller.actionutil.ParamDef.DataType;
import org.gusdb.wdk.controller.actionutil.ParamDef.Required;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.controller.actionutil.WdkAction;
import org.gusdb.wdk.model.user.analysis.StepAnalysisContext;
import org.gusdb.wdk.model.user.analysis.StepAnalysisFactory;

public class CreateStepAnalysisAction extends WdkAction {

  private static final String ANALYSIS_NAME_KEY = "analysisName";
  private static final String STRATEGY_ID_KEY = "strategyId";
  private static final String STEP_ID_KEY = "stepId";

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
    
    context = analysisMgr.createAnalysis(context);

    return AbstractStepAnalysisIdAction.getStepAnalysisJsonResult(context);
  }
}
