package org.gusdb.wdk.controller.action;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

/**
 * form bean for holding the boolean expression string fro queryHistory.jsp page
 */

public class BooleanExpressionForm extends ActionForm {

    /**
     * 
     */
    private static final long serialVersionUID = -6371621860440022826L;
    private String booleanExpression = null;
    private String historySectionId = null;

    public void setBooleanExpression(String be) {
        booleanExpression = be;
    }

    public String getBooleanExpression() {
        return booleanExpression;
    }

    public void setHistorySectionId(String si) {
        historySectionId = si;
    }

    public String getHistorySectionId() {
        return historySectionId;
    }

    /**
     * validate the properties that have been sent from the HTTP request, and
     * return an ActionErrors object that encapsulates any validation errors
     */
    public ActionErrors validate(ActionMapping mapping,
            HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        WdkModelBean wdkModel = (WdkModelBean) getServlet().getServletContext().getAttribute(
                CConstants.WDK_MODEL_KEY);
        Map<String, String> operatorMap = wdkModel.getBooleanOperators();

        String errMsg = null;
        try {
            UserBean wdkUser = ActionUtility.getUser(getServlet(), request);
            errMsg = wdkUser.validateExpression(getBooleanExpression(),
                    operatorMap);
            if (errMsg != null) {
                errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
                        "mapped.properties", "booleanExpression", errMsg));
            }
        } catch (WdkModelException exp) {
            errMsg = exp.getMessage();
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
                    "mapped.properties", "booleanExpression", errMsg));
        } catch (WdkUserException ex) {
            throw new RuntimeException(ex);
        }

        return errors;
    }
}
