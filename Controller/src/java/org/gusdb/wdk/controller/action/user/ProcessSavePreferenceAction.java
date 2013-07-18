/**
 * 
 */
package org.gusdb.wdk.controller.action.user;

import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamDef;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.controller.actionutil.WdkAction;
import org.gusdb.wdk.model.jspwrap.UserBean;

/**
 * @author xingao
 * @author rdoherty
 */
public class ProcessSavePreferenceAction extends WdkAction {

    private static Logger logger = Logger.getLogger(ProcessSavePreferenceAction.class.getName());

    //  since params are dynamic, do not validate
    @Override protected boolean shouldValidateParams() { return false; }
    @Override protected Map<String, ParamDef> getParamDefs() { return null; }

    @Override
    protected ActionResult handleRequest(ParamGroup params) throws Exception {

      UserBean wdkUser = getCurrentUser();
      
      for (String key : params.getKeys()) {
        String value = params.getValue(key);
        if (key.startsWith(CConstants.WDK_PREFERENCE_GLOBAL_KEY)) {
          wdkUser.setGlobalPreference(key, value);
          logger.info("Saving user " + wdkUser.getEmail() +
              "'s reference " + key + "=" + value);
        }
        else if (key.startsWith(CConstants.WDK_PREFERENCE_PROJECT_KEY)) {
          wdkUser.setProjectPreference(key, value);
          logger.info("Saving user " + wdkUser.getEmail() +
              "'s reference " + key + "=" + value);
        }
      }

      wdkUser.save();
      return new ActionResult().setRedirect(true).setViewPath("/");
    }
}
