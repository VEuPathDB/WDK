package org.gusdb.wdk.controller.wizard;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionServlet;
import org.gusdb.wdk.controller.form.WizardForm;

public interface StageHandler {

    /**
     * @param servlet
     * @param request
     * @param response
     * @param wizardForm
     * @return return extra param values that should be set into attributes if
     *         going to view, or put into forward link if going to an action.
     * @throws Exception if error occurs
     */
    public Map<String, Object> execute(ActionServlet servlet,
            HttpServletRequest request, HttpServletResponse response,
            WizardForm wizardForm) throws Exception;
}
