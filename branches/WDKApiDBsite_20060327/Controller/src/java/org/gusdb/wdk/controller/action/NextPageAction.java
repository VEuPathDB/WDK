package org.gusdb.wdk.controller.action;

import java.util.Map;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.HashMap;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionServlet; 

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.jspwrap.EnumParamBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.AnswerBean;
import org.gusdb.wdk.model.jspwrap.BooleanQuestionLeafBean;

/** NextPageAction.java
* Created to handle multi-page display for custom specifications 
* @author Nivedita Kaluskar
**/

public class NextPageAction extends Action {
    public ActionForward execute(ActionMapping mapping,
				 ActionForm form,
				 HttpServletRequest request,
				 HttpServletResponse response) throws Exception {

	ActionForward forward = null;
	String  submitAction = request.getParameter(CConstants.PQ_NEXT_PAGE_KEY);
      System.out.println("submitAction : " +submitAction);


       if (submitAction.equals(CConstants.PQ_PAGE3)){
           forward = mapping.findForward(CConstants.PQ_PAGE3_MAPKEY);
       }
        else if (submitAction.equals(CConstants.PQ_PAGE2)){
           forward = mapping.findForward(CConstants.PQ_PAGE2_MAPKEY);
       }
        else if (submitAction.equals(CConstants.PQ_PAGE1)){
           forward = mapping.findForward(CConstants.PQ_PAGE1_MAPKEY);
       }
        else if (submitAction.equals(CConstants.PQ_NEXT_PAGE)){
	    forward = mapping.findForward(CConstants.PQ_NEXT_PAGE_MAPKEY);
       }

	return forward;
    }
}
