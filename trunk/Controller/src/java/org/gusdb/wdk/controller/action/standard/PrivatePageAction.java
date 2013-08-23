package org.gusdb.wdk.controller.action.standard;

public class PrivatePageAction extends GenericPageAction {

	@Override
	protected boolean requiresLogin() {
		return true;
	}
}
