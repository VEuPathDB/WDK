package org.gusdb.wdk.controller.action;

import org.apache.struts.action.ActionForm;
import org.gusdb.wdk.model.WdkModelException;


/**
 *  form bean for holding the boolean expression string fro queryHistory.jsp page
 */

public class BooleanExpressionForm extends ActionForm {

    private String booleanExpression = null;

    void setBooleanExpression(String be) { booleanExpression = be; }

    String getBooleanExpression() { return booleanExpression; }
}
