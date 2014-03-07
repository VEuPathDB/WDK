package org.gusdb.wdk.controller.action.analysis;

import java.util.List;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.IoUtil;
import org.gusdb.wdk.controller.action.standard.GenericPageAction;
import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.controller.actionutil.ResponseType;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.analysis.StepAnalysisContext;
import org.gusdb.wdk.model.user.analysis.StepAnalysisFactory;
import org.json.JSONObject;

public class RunStepAnalysisAction extends GenericPageAction {

  private static final Logger LOG = Logger.getLogger(RunStepAnalysisAction.class);
  
  @Override
  protected ActionResult handleRequest(ParamGroup params) throws Exception {

    StepAnalysisContext context = new StepAnalysisContext(getCurrentUser(), params.getParamMap());
    StepAnalysisFactory analysisMgr = getWdkModel().getModel().getStepAnalysisFactory();
    
    JSONObject json = new JSONObject();
    List<String> errors = analysisMgr.validateFormParams(context);
    if (errors.isEmpty()) {
      try {
        analysisMgr.applyAnalysis(context);
        json.put("status", "success");
        json.put("analysisId", context.getAnalysisId());
        json.put("displayName", context.getDisplayName());
        json.put("description", context.getStepAnalysis().getDescription());
      }
      catch (WdkModelException e) {
        LOG.error("Unable to execute step analysis with name " + context.getStepAnalysis().getName());
        json.put("status", "error");
        json.put("error", e.getMessage());
      }
    }
    else {
      // bad param values
      json.put("status", "validation");
      for (String error : errors) {
        json.append("error", error);
      }
    }
    
    return new ActionResult(ResponseType.json)
        .setStream(IoUtil.getStreamFromString(json.toString()));
  }
}
