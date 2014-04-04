package org.gusdb.wdk.controller.action.analysis;

import java.util.List;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.IoUtil;
import org.gusdb.wdk.controller.action.standard.GenericPageAction;
import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.controller.actionutil.ResponseType;
import org.gusdb.wdk.model.user.analysis.StepAnalysisContext;
import org.gusdb.wdk.model.user.analysis.StepAnalysisFactory;
import org.json.JSONObject;

public class RunStepAnalysisAction extends GenericPageAction {

  @SuppressWarnings("unused")
  private static final Logger LOG = Logger.getLogger(RunStepAnalysisAction.class);
  
  private static enum JsonKey { status, context, errors }
  
  @Override
  protected ActionResult handleRequest(ParamGroup params) throws Exception {
    
    StepAnalysisFactory analysisMgr = getWdkModel().getModel().getStepAnalysisFactory();
    StepAnalysisContext context = StepAnalysisContext.createFromForm(params.getParamMap(), analysisMgr);
    AbstractStepAnalysisIdAction.verifyOwnership(getCurrentUser(), context);
    List<String> errors = analysisMgr.validateFormParams(context);

    JSONObject json = new JSONObject();
    if (errors.isEmpty()) {
      context = analysisMgr.runAnalysis(context);
      json.put(JsonKey.status.name(), "success");
      json.put(JsonKey.context.name(), context.getInstanceJson());
    }
    else {
      // bad param values
      json.put(JsonKey.status.name(), "validation");
      for (String error : errors) {
        json.append(JsonKey.errors.name(), error);
      }
    }
    
    return new ActionResult(ResponseType.json)
        .setStream(IoUtil.getStreamFromString(json.toString()));
  }
}
