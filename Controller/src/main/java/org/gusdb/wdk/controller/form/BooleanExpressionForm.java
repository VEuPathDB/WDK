package org.gusdb.wdk.controller.form;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.gusdb.wdk.controller.actionutil.ActionUtility;
import org.gusdb.wdk.model.jspwrap.UserBean;

/**
 * form bean for holding the boolean expression string for queryStep.jsp page
 */
public class BooleanExpressionForm extends ActionForm {

    private static final long serialVersionUID = -6371621860440022826L;

    private static Logger logger = Logger.getLogger(BooleanExpressionForm.class);

    private String booleanExpression = null;
    private String userAnswerSectionId = null;

    private boolean useBooleanFilter = true;

    public void setBooleanExpression(String be) {
        booleanExpression = be;
    }

    public String getBooleanExpression() {
        return booleanExpression;
    }

    public void setStepSectionId(String si) {
        userAnswerSectionId = si;
    }

    public String getStepSectionId() {
        return userAnswerSectionId;
    }

    /**
     * @return the useBooleanFilter
     */
    public boolean isUseBooleanFilter() {
        return useBooleanFilter;
    }

    /**
     * @param useBooleanFilter
     *            the useBooleanFilter to set
     */
    public void setUseBooleanFilter(boolean useBooleanFilter) {
        this.useBooleanFilter = useBooleanFilter;
    }

    /**
     * validate the properties that have been sent from the HTTP request, and
     * return an ActionErrors object that encapsulates any validation errors
     */
    @Override
    public ActionErrors validate(ActionMapping mapping,
            HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        try {
            UserBean wdkUser = ActionUtility.getUser(getServlet(), request);
            String expression = getBooleanExpression();

            logger.info("Validating boolean expression: " + expression);

            wdkUser.validateExpression(expression);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionMessage(
                    "mapped.properties", "booleanExpression", ex.getMessage()));
        }

        return errors;
    }
}
