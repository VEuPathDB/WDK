package org.gusdb.wdk.controller.action.analysis;

import java.util.HashMap;
import java.util.Map;

import org.gusdb.fgputil.IoUtil;
import org.gusdb.wdk.controller.action.standard.GenericPageAction;
import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.controller.actionutil.ResponseType;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.analysis.AnalysisResult;
import org.gusdb.wdk.model.user.analysis.ExecutionStatus;
import org.gusdb.wdk.model.user.analysis.StepAnalysisContext;
import org.gusdb.wdk.model.user.analysis.StepAnalysisFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GetAllStepAnalysisAction extends GenericPageAction {

  private static class Analysis {
    public StepAnalysisContext context;
    public AnalysisResult result;
    public Analysis(StepAnalysisContext context, AnalysisResult result) {
      this.context = context; this.result = result;
    }
  }
  
  @Override
  protected ActionResult handleRequest(ParamGroup params) throws Exception {
    
    StepAnalysisFactory analysisMgr = getWdkModel().getModel().getStepAnalysisFactory();
    Map<Integer, Analysis> analysisMap = new HashMap<>();
    for (StepAnalysisContext context : analysisMgr.getAllAnalyses()) {
      AnalysisResult result = (context.getStatus().equals(ExecutionStatus.COMPLETE) ?
          analysisMgr.getAnalysisResult(context) : null);
      analysisMap.put(context.getAnalysisId(), new Analysis(context, result));
    }
    return createJsonResult(analysisMap);
  }

  private ActionResult createJsonResult(Map<Integer, Analysis> analysisMap) throws WdkModelException {
    try {
      JSONArray json = new JSONArray();
      for (int id : analysisMap.keySet()) {
        Analysis analysis = analysisMap.get(id);
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("context", analysis.context.getInstanceJson());
        if (analysis.context.getStatus().equals(ExecutionStatus.COMPLETE)) {
          obj.put("resultStatus", analysis.result.getStatus());
          obj.put("storedString", analysis.result.getStoredString());
          byte[] storedBytes = analysis.result.getStoredBytes();
          obj.put("storedBytesSize", storedBytes == null ? 0 : storedBytes.length);
        }
        json.put(obj);
      }
      return new ActionResult(ResponseType.json)
          .setStream(IoUtil.getStreamFromString(json.toString()));
    }
    catch (JSONException e) {
      throw new WdkModelException ("Unable to create comprehensive json object.");
    }
  }

}
