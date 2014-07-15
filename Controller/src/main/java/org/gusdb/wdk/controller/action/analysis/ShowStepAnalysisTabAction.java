package org.gusdb.wdk.controller.action.analysis;

import java.util.Map;

import org.gusdb.fgputil.MapBuilder;
import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamDef;
import org.gusdb.wdk.controller.actionutil.ParamDef.DataType;
import org.gusdb.wdk.controller.actionutil.ParamDef.Required;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.controller.actionutil.WdkAction;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.Strategy;

public class ShowStepAnalysisTabAction extends WdkAction {

  private static final String STRATEGY_ID_KEY = "strategy";
  private static final String STEP_ID_KEY = "step";
  
  private static final Map<String, ParamDef> PARAMS = new MapBuilder<String, ParamDef>()
      .put(STRATEGY_ID_KEY, new ParamDef(Required.REQUIRED, DataType.INTEGER))
      .put(STEP_ID_KEY, new ParamDef(Required.REQUIRED, DataType.INTEGER))
      .toMap();
  
  @Override protected boolean shouldValidateParams() { return true; }
  @Override protected Map<String, ParamDef> getParamDefs() { return PARAMS; }

  @Override
  protected ActionResult handleRequest(ParamGroup params) throws Exception {
    int strategyId = params.getIntValue(STRATEGY_ID_KEY);
    int stepId = params.getIntValue(STEP_ID_KEY);
    
    Strategy strategy = getWdkModel().getModel().getStepFactory().getStrategyById(strategyId);
    Step step = strategy.getStepById(stepId);

    if (step == null) {
      throw new WdkUserException("No step bean exists with id " + stepId + " on " +
            "strategy with id " + strategyId + " for user " + getCurrentUser().getUserId());
    }
    
    return new ActionResult().setViewName(SUCCESS)
        .setRequestAttribute("wdkStrategy", strategy)
        .setRequestAttribute("wdkStep", step);
  }
}
