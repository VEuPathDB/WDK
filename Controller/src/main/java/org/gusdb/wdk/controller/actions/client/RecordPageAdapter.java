package org.gusdb.wdk.controller.actions.client;

import java.util.Map;
import java.util.Set;

import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamDef;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.controller.actionutil.WdkAction;

/**
 * Redirect the browser to the SOA Record Page. This simply maps urls, without
 * providing any other logic.
 * 
 * @author dfalke
 */
public class RecordPageAdapter extends WdkAction {

  private static final String PARAM_RECORD_CLASS_NAME = "name";

  @Override
  protected boolean shouldValidateParams() {
    return false;
  }

  @Override
  protected Map<String, ParamDef> getParamDefs() {
    return null;
  }

  @Override
  protected ActionResult handleRequest(ParamGroup params) throws Exception {
    String url = createUrl(getWebAppRoot(), params);
    return new ActionResult().setExternalPath(url);
  }
  
  private static String createUrl(String webAppRoot, ParamGroup params) {
    String path = "/app/record/" + params.getValue(PARAM_RECORD_CLASS_NAME);
    String query = "";
    Set<String> paramKeys = params.getKeys();
    paramKeys.remove(PARAM_RECORD_CLASS_NAME);
    for (String key: paramKeys) {
      query += query.length() == 0 ? "?" : "&";
      query += key + "=" + params.getValue(key);
    }
    return webAppRoot + path + query;
  }
}
