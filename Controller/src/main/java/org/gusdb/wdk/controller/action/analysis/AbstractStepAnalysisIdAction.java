package org.gusdb.wdk.controller.action.analysis;

import java.util.HashMap;
import java.util.Map;

import org.gusdb.fgputil.IoUtil;
import org.gusdb.fgputil.MapBuilder;
import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamDef;
import org.gusdb.wdk.controller.actionutil.ParamDef.DataType;
import org.gusdb.wdk.controller.actionutil.ParamDef.Required;
import org.gusdb.wdk.controller.actionutil.ResponseType;
import org.gusdb.wdk.controller.actionutil.WdkAction;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.user.analysis.StepAnalysisInstance;
import org.gusdb.wdk.model.user.analysis.StepAnalysisFactory;

public abstract class AbstractStepAnalysisIdAction extends WdkAction {
  
  private static final Map<String,ParamDef> PARAMS =
      new MapBuilder<String, ParamDef>().put(StepAnalysisInstance.ANALYSIS_ID_KEY,
          new ParamDef(Required.REQUIRED, DataType.LONG)).toMap();
  
  private StepAnalysisFactory _analysisMgr;
  
  protected Map<String, ParamDef> getAdditionalParams() {
    return null;
  }
  
  @Override
  protected final boolean shouldValidateParams() {
    return true;
  }
  
  @Override
  protected final Map<String, ParamDef> getParamDefs() {
    Map<String, ParamDef> additionalParams = getAdditionalParams();
    if (additionalParams != null) {
      Map<String, ParamDef> params = new HashMap<>(PARAMS);
      params.putAll(additionalParams);
      return params;
    }
    return PARAMS;
  }
  
  protected final long getAnalysisId() {
    return getParams().getLongValue(StepAnalysisInstance.ANALYSIS_ID_KEY);
  }
  
  protected final StepAnalysisFactory getAnalysisMgr() {
    if (_analysisMgr == null) {
      _analysisMgr = getWdkModel().getModel().getStepAnalysisFactory();
    }
    return _analysisMgr;
  }
  
  protected final StepAnalysisInstance getContextFromPassedId()
      throws WdkUserException, WdkModelException {
    StepAnalysisInstance context = StepAnalysisInstance.createFromId(getAnalysisId(), getAnalysisMgr());
    verifyOwnership(getCurrentUser(), context);
    return context;
  }
  
  public static ActionResult getStepAnalysisJsonResult(StepAnalysisInstance context) {
    return new ActionResult(ResponseType.json).setStream(
        IoUtil.getStreamFromString(context.getJsonSummary().toString()));
  }

  public static void verifyOwnership(UserBean user, StepAnalysisInstance context)
      throws WdkUserException, WdkModelException {
    if (user.getUserId() != context.getStep().getUser().getUserId()) {
      throw new WdkUserException("Permisssion Denied: You do not have permission" +
      		" to operate on the analysis with id " + context.getAnalysisId());
    }
  }
}
