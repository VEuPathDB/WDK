package org.gusdb.wdk.controller.wizard;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.gusdb.wdk.model.WdkModel;

public interface StageHandler {

    /**
     * @param wdkModel
     * @param request
     * @param response
     * @param wizardForm
     * @return return extra param values that should be set into attributes if
     *         going to view, or put into forward link if going to an action.
     * @throws Exception if error occurs
     */
    public Map<String, Object> execute(WdkModel wdkModel, HttpServletRequest request,
        HttpServletResponse response, WizardFormIfc wizardForm) throws Exception;
}
