package org.gusdb.wdk.controller.action.analysis;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.Map;

import org.gusdb.fgputil.MapBuilder;
import org.gusdb.wdk.controller.WdkValidationException;
import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamDef;
import org.gusdb.wdk.controller.actionutil.ParamDef.Required;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.controller.actionutil.ResponseType;

public class GetStepAnalysisResourceAction extends AbstractStepAnalysisIdAction {

  public static final String RESOURCE_PATH_KEY = "path";
  
  private static final Map<String,ParamDef> EXTRA_PARAMS = new MapBuilder<String, ParamDef>()
      .put(RESOURCE_PATH_KEY, new ParamDef(Required.REQUIRED)).toMap();
  
  @Override
  protected Map<String, ParamDef> getAdditionalParams() {
    return EXTRA_PARAMS;
  }
  
  @Override
  public void performAdditionalValidation(ParamGroup params) throws WdkValidationException {
    String path = params.getValue(RESOURCE_PATH_KEY);
    if (path.contains("..")) throw new WdkValidationException("Illegal path: cannot contain '..'.");
  }
  
  @Override
  protected ActionResult handleRequest(ParamGroup params) throws Exception {
    Path resourcePath = getAnalysisMgr().getResourcePath(
        getContextFromPassedId(), params.getValue(RESOURCE_PATH_KEY));
    File resourceFile = resourcePath.toFile();
    if (resourceFile.exists() && resourceFile.isFile() && resourceFile.canRead()) {
      return new ActionResult(ResponseType.resolveType(resourcePath))
          .setStream(new BufferedInputStream(new FileInputStream(resourceFile)));
    }
    return new ActionResult().setHttpResponseStatus(404);
  }
}
