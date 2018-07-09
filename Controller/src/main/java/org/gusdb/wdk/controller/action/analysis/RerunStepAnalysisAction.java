package org.gusdb.wdk.controller.action.analysis;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.IoUtil;
import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.controller.actionutil.ResponseType;
import org.gusdb.wdk.model.user.analysis.StepAnalysisInstance;
import org.json.JSONObject;

public class RerunStepAnalysisAction extends AbstractStepAnalysisIdAction {

  @SuppressWarnings("unused")
  private static final Logger LOG = Logger.getLogger(RerunStepAnalysisAction.class);

  private static enum JsonKey { status, context }

  @Override
  protected ActionResult handleRequest(ParamGroup params) throws Exception {
    StepAnalysisInstance context = getContextFromPassedId();
    JSONObject json = new JSONObject();
    if (context.hasParams() || !context.getStepAnalysis().getHasParameters()) {
      context = getAnalysisMgr().runAnalysis(context);
      json.put(JsonKey.status.name(), "success");
      json.put(JsonKey.context.name(), context.getJsonSummary());
    }
    else {
      // cannot rerun analysis that has not yet submitted params
      json.put(JsonKey.status.name(), "no_params");
    }
    
    return new ActionResult(ResponseType.json)
        .setStream(IoUtil.getStreamFromString(json.toString()));
  }
}
