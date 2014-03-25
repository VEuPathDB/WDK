package org.gusdb.wdk.controller.action.analysis;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.gusdb.fgputil.IoUtil;
import org.gusdb.wdk.controller.action.standard.GenericPageAction;
import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.controller.actionutil.ResponseType;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.analysis.StepAnalysisContext;
import org.gusdb.wdk.model.user.analysis.StepAnalysisFactory;
import org.gusdb.wdk.model.user.analysis.StepAnalysisFactory.AnalysisResult;
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
      analysisMap.put(context.getAnalysisId(),
          new Analysis(context, analysisMgr.getAnalysisResult(context)));
    }
    return createJsonResult(analysisMap);
  }

  private ActionResult createJsonResult(Map<Integer, Analysis> analysisMap) throws WdkModelException {
    try {
      JSONArray json = new JSONArray();
      for (Entry<Integer,Analysis> entry : analysisMap.entrySet()) {
        JSONObject obj = new JSONObject();
        obj.put("id", entry.getKey());
        obj.put("context", entry.getValue().context.getInstanceJson());
        obj.put("resultStatus", entry.getValue().result.status);
        obj.put("resultData", entry.getValue().result.serializedResult);
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
