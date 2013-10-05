package org.gusdb.wdk.controller.action;

import java.util.List;
import java.util.Map;

import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamDef;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.controller.actionutil.WdkAction;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.Strategy;

public class ShowPublicStratsAction extends WdkAction {

  @Override protected boolean shouldValidateParams() { return false; }
  @Override protected Map<String, ParamDef> getParamDefs() { return null; }

  @Override
  protected ActionResult handleRequest(ParamGroup params) throws Exception {
    List<Strategy> publicStrats = getWdkModel().getModel().getStepFactory().loadPublicStrategies();
    return new ActionResult().setViewName(SUCCESS)
        .setRequestAttribute("publicStrats", publicStrats)
        .setRequestAttribute("numValidPublicStrats", getNumValid(publicStrats));
  }

  private int getNumValid(List<Strategy> stratList) throws WdkModelException {
	int numValid = 0;
	for (Strategy strat : stratList) {
      if (strat.isValid()) numValid++;
	}
	return numValid;
  }
}
