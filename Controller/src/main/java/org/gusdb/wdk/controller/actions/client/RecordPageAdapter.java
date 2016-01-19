package org.gusdb.wdk.controller.actions.client;

import java.util.Map;
import java.util.Set;

import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamDef;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.controller.actionutil.WdkAction;
import org.gusdb.wdk.model.jspwrap.RecordClassBean;

/**
 * Redirect the browser to the SOA Record Page. This simply maps urls, without
 * providing any other logic.
 * 
 * @author dfalke
 */
public class RecordPageAdapter extends WdkAction {

  private static final String PARAM_RECORD_CLASS_NAME = "name";
  private static final String PARAM_PRIMARY_KEY = "primary_key";

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
    String recordClassRef = params.getValue(PARAM_RECORD_CLASS_NAME);
    RecordClassBean recordClass = getWdkModel().findRecordClass(recordClassRef);
    String url = createUrl(getWebAppRoot(), recordClass, params);
    return new ActionResult().setExternalPath(url);
  }
  
  private static String createUrl(String webAppRoot, RecordClassBean recordClass, ParamGroup params) {
    String path = "/app/record/" + recordClass.getUrlSegment();
    Set<String> paramKeys = params.getKeys();
    paramKeys.remove(PARAM_RECORD_CLASS_NAME);

    // support urls of the form ?primary_key={primaryKeyValue}
    if (paramKeys.contains(PARAM_PRIMARY_KEY)) {
      path += "/" + params.getValue(PARAM_PRIMARY_KEY);
    }

    // treat each query param as a part of the primary key
    else {
      for (String key: recordClass.getPrimaryKeyColumns()) {
        path += "/" + params.getValue(key);
      }
    }

    return webAppRoot + path;
  }
}
