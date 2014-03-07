package org.gusdb.wdk.controller.action.analysis;

import java.util.Map;

import org.apache.log4j.Logger;
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
import org.gusdb.wdk.model.user.analysis.StepAnalysisFactory;
import org.json.JSONObject;

public class DeleteStepAnalysisAction extends WdkAction {

  private static final Logger LOG = Logger.getLogger(DeleteStepAnalysisAction.class);
  
  private static final String STEP_ANALYSIS_ID_KEY = "stepAnalysisId";
  
  @Override protected boolean shouldValidateParams() { return true; }
  @Override protected Map<String, ParamDef> getParamDefs() {
    return new MapBuilder<String, ParamDef>().put(STEP_ANALYSIS_ID_KEY,
        new ParamDef(Required.REQUIRED, DataType.INTEGER)).toMap(); }
  
  @Override
  protected ActionResult handleRequest(ParamGroup params) throws Exception {
    
    int analysisId = params.getIntValue(STEP_ANALYSIS_ID_KEY);
    StepAnalysisFactory analysisMgr = getWdkModel().getModel().getStepAnalysisFactory();

    JSONObject json = new JSONObject();
    try {
      analysisMgr.deleteAnalysis(analysisId);
      json.put("status", "success");
    }
    catch (WdkModelException e) {
      LOG.error("Unable to delete step analysis with id " + analysisId, e);
      json.put("status", "error");
      json.put("error", e.getMessage());
    }
    
    return new ActionResult(ResponseType.json)
        .setStream(IoUtil.getStreamFromString(json.toString()));
  }

}
