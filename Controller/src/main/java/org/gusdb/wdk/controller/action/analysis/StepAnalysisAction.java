package org.gusdb.wdk.controller.action.analysis;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.wdk.controller.action.standard.GenericPageAction;
import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.HttpMethod;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.controller.actionutil.RequestData;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.analysis.StepAnalysisContext;

/**
 * NOTE!! This is an incomplete idea to convert the step analysis interface into
 * a truly RESTful service.  No URLs currently map to this action.  The idea may
 * be explored later but for now this class is not in use.
 * 
 * @author rdoherty
 */
public class StepAnalysisAction extends GenericPageAction {

  @SuppressWarnings("unused")
  private static final Logger LOG = Logger.getLogger(CreateStepAnalysisAction.class);

  /**
   * Supports RESTful access to step analysis executions:
   * 
   *   Verbs          Path                        Type
   *   PUT:           /stepAnalysis               json
   *   GET, DELETE:   /stepAnalysis/{id}          json
   *   GET:           /stepAnalysis/{id}/form     html
   *   PUT, GET:      /stepAnalysis/{id}/params   form-submit, json
   *   GET:           /stepAnalysis/{id}/result   html
   */
  @Override
  protected ActionResult handleRequest(ParamGroup params) throws Exception {
    
    // dispatch to handler based on URL and method
    RequestData req = getRequestData();
    HttpMethod method = req.getMethod();
    String[] path = req.getRequestUri().substring(1).split("/");
    
    if (path.length < 1 || !path[0].equals("stepAnalysis")) {
      handleUnknownPath("Unsupported URI: " + req.getRequestUri());
    }

    // FIXME: THIS WILL NOT WORK!!!
    StepAnalysisContext context = StepAnalysisContext.createFromForm(params.getParamMap(),
        getWdkModel().getModel().getStepAnalysisFactory());
    
    if (path.length == 1) {
      return getNewAnalysisResponse(method, context);
    }
    
    int analysisId = -1;
    if (!FormatUtil.isInteger(path[1]) || (analysisId = Integer.parseInt(path[1])) < 1) {
      handleUnknownPath("Illegal step analysis ID: " + path[1]);
    }
    context.setAnalysisId(analysisId);
    
    switch (path.length) {
      case 2:
        return getStepAnalysisResponse(method, context);
      case 3:
        switch(path[2]) {
          case "form":
            return getFormResponse(method, context);
          case "params":
            return getParamsResponse(method, context);
          case "result":
            return getResultResponse(method, context);
        }
      default:
        handleUnknownPath("Unknown URI: " + req.getRequestUri());
        return null;
    }
  }
  
  private ActionResult getNewAnalysisResponse(HttpMethod method, StepAnalysisContext context) {
    // TODO Auto-generated method stub
    return null;
  }
  
  private ActionResult getStepAnalysisResponse(HttpMethod method, StepAnalysisContext context) {
    // TODO Auto-generated method stub
    return null;
  }

  private ActionResult getFormResponse(HttpMethod method, StepAnalysisContext context) {
    // TODO Auto-generated method stub
    return null;
  }

  private ActionResult getParamsResponse(HttpMethod method, StepAnalysisContext context) {
    // TODO Auto-generated method stub
    return null;
  }

  private ActionResult getResultResponse(HttpMethod method, StepAnalysisContext context) {
    // TODO Auto-generated method stub
    return null;
  }

  private void handleUnknownPath(String message) throws WdkUserException {
    throw new WdkUserException(message);
  }
}
