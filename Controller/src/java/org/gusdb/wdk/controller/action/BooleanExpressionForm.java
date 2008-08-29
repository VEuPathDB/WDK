package org.gusdb.wdk.controller.action;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.model.jspwrap.UserBean;

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

    private String summaryTable = null;
    private String viewRow = null;
    private String viewColumn = null;

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
     * @return the summaryTable
     */
    public String getSummaryTable() {
        return summaryTable;
    }

    /**
     * @param summaryTable
     *            the summaryTable to set
     */
    public void setSummaryTable(String summaryTable) {
        this.summaryTable = summaryTable;
    }

    /**
     * @return the viewRow
     */
    public String getViewRow() {
        return viewRow;
    }

    /**
     * @param viewRow
     *            the viewRow to set
     */
    public void setViewRow(String viewRow) {
        this.viewRow = viewRow;
    }

    /**
     * @return the viewColumn
     */
    public String getViewColumn() {
        return viewColumn;
    }

    /**
     * @param viewColumn
     *            the viewColumn to set
     */
    public void setViewColumn(String viewColumn) {
        this.viewColumn = viewColumn;
    }

    /**
     * validate the properties that have been sent from the HTTP request, and
     * return an ActionErrors object that encapsulates any validation errors
     */
    public ActionErrors validate(ActionMapping mapping,
            HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        String errMsg = null;
        try {
            UserBean wdkUser = ActionUtility.getUser(getServlet(), request);
            wdkUser.validateExpression(getBooleanExpression());
            if (errMsg != null) {
                errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
                        "mapped.properties", "booleanExpression", errMsg));
            }
        } catch (Exception exp) {
            errMsg = exp.getMessage();
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
                    "mapped.properties", "booleanExpression", errMsg));
        }

        return errors;
    }
}
