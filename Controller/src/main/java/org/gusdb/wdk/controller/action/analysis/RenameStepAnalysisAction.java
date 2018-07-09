package org.gusdb.wdk.controller.action.analysis;

import java.util.Map;

import org.gusdb.fgputil.MapBuilder;
import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamDef;
import org.gusdb.wdk.controller.actionutil.ParamDef.Required;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.model.user.analysis.StepAnalysisInstance;

public class RenameStepAnalysisAction extends AbstractStepAnalysisIdAction {

  public static final String DISPLAY_NAME_KEY = "displayName";
  
  private static final Map<String,ParamDef> EXTRA_PARAMS = new MapBuilder<String, ParamDef>()
      .put(DISPLAY_NAME_KEY, new ParamDef(Required.REQUIRED)).toMap();

  @Override
  protected Map<String, ParamDef> getAdditionalParams() {
    return EXTRA_PARAMS;
  }
  
  @Override
  protected ActionResult handleRequest(ParamGroup params) throws Exception {
    StepAnalysisInstance context = getContextFromPassedId();
    context.setDisplayName(params.getValue(DISPLAY_NAME_KEY));
    getAnalysisMgr().renameInstance(context);
    return getStepAnalysisJsonResult(context);
  }
  
}
