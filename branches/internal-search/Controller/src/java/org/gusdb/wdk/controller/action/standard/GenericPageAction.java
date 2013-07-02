package org.gusdb.wdk.controller.action.standard;

import java.util.Map;

import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamDef;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.controller.actionutil.WdkAction;

public class GenericPageAction extends WdkAction {

  // no params expected or required
  @Override protected boolean shouldValidateParams() { return false; }
	@Override protected Map<String, ParamDef> getParamDefs() { return null; }

	@Override
	protected ActionResult handleRequest(ParamGroup params) throws Exception {
		// take no action
		return new ActionResult().setViewName(SUCCESS);
	}
}
