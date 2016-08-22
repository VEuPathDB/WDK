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
    String url = createUrl(recordClass, params.getParamMap());
    return new ActionResult().setExternalPath(url);
  }
  
  public static String createUrl(RecordClassBean recordClass, Map<String, String[]> params) {
    String path = "/record/" + recordClass.getUrlSegment();
    Set<String> paramKeys = params.keySet();
    paramKeys.remove(PARAM_RECORD_CLASS_NAME);

    // support urls of the form ?primary_key={primaryKeyValue}
    if (paramKeys.contains(PARAM_PRIMARY_KEY)) {
      path += "/" + params.get(PARAM_PRIMARY_KEY)[0];
    }

    // treat each query param as a part of the primary key
    else {
      for (String key: recordClass.getPrimaryKeyColumns()) {
        if (params.containsKey(key))
          path += "/" + params.get(key)[0];
      }
    }

    return path;
  }
}
