package org.gusdb.wdk.controller.wizard;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionServlet;
import org.gusdb.wdk.controller.action.WizardForm;

public interface StageHandler {

    public void execute(ActionServlet servlet, HttpServletRequest request,
            HttpServletResponse response, WizardForm wizardForm) throws Exception;
}
