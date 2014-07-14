package org.gusdb.wdk.controller.actionutil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.model.WdkRuntimeException;

/**
 * Go-between class that allows Struts1 to call non-static actions that can
 * contain state.  In struts-config.xml, this action can be assigned the 'type'
 * attribute of an action.  It reads the parameter attribute of the action,
 * which should contain the fully qualified name of a subclass of WdkAction.
 * This action then instantiates and delegates the processing of the request to
 * the child class and returns the ActionForward produced by that instance.
 * 
 * @author rdoherty
 */
public class WdkActionWrapper extends Action {
	
	private static final Logger LOG = Logger.getLogger(WdkActionWrapper.class.getName());

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		try {
			String actionClassName = mapping.getParameter();
			if (actionClassName == null || actionClassName.length() == 0) {
				throw new WdkRuntimeException("No action class is specified in struts-config.xml " +
						"(use 'parameter' attribute of the action tag to specify WdkAction class).");
			}
			try {
				Class<?> actionClass = Class.forName(actionClassName);
				if (WdkAction.class.isAssignableFrom(actionClass)) {
					WdkAction action = (WdkAction)actionClass.newInstance();
					return action.execute(mapping, form, request, response, getServlet());
				}
				throw new WdkRuntimeException("The class specified in struts-config.xml (" +
						actionClassName + ") is not a subclass of " + WdkAction.class.getName());
			}
			catch (LinkageError | ClassNotFoundException e) {
				throw new WdkRuntimeException("The class specified in struts-config.mxl (" +
						actionClassName + ") cannot be found or has failed to initialize.", e);
			}
		}
		catch (WdkRuntimeException e) {
			LOG.error("Error during action execution.", e);
			throw e;
		}
	}
}
