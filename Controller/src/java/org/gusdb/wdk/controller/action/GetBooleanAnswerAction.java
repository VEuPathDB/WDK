package org.gusdb.wdk.controller.action;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.BooleanQuery;
import org.gusdb.wdk.model.jspwrap.AnswerBean;
import org.gusdb.wdk.model.jspwrap.BooleanQuestionLeafBean;
import org.gusdb.wdk.model.jspwrap.BooleanQuestionNodeBean;
import org.gusdb.wdk.model.jspwrap.ParamBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.UserBean;


/**
 * This Action is called by the ActionServlet when a WDK question is asked.
 * It 1) reads param values from input form bean,
 *    2) runs the query and saves the answer
 *    3) forwards control to a jsp page that displays a summary
 */

public class GetBooleanAnswerAction extends ShowSummaryAction {
    public ActionForward execute(ActionMapping mapping,
				 ActionForm form,
				 HttpServletRequest request,
				 HttpServletResponse response) throws Exception {


	BooleanQuestionForm bqf = (BooleanQuestionForm)form;

	Object root = request.getSession().getAttribute(CConstants.CURRENT_BOOLEAN_ROOT_KEY);
	AnswerBean answer;
	if (root instanceof BooleanQuestionLeafBean) {
	    BooleanQuestionLeafBean rootLeaf = (BooleanQuestionLeafBean)root;
	    QuestionBean leafQuestion = rootLeaf.getQuestion();
	    Map params = getParamsFromForm(bqf, rootLeaf);
	    answer = summaryPaging(request, leafQuestion, params);
	} else {
	    BooleanQuestionNodeBean rootNode = (BooleanQuestionNodeBean)root;
	    Vector allNodes = new Vector();
	    allNodes = rootNode.getAllNodes(allNodes);
	
	    for (int i = 0; i < allNodes.size(); i++){
		Object nextNode = allNodes.elementAt(i);
		if (nextNode instanceof BooleanQuestionLeafBean){
		
		    BooleanQuestionLeafBean nextLeaf = (BooleanQuestionLeafBean)nextNode;
            LinkedHashMap values = getParamsFromForm(bqf, nextLeaf);
		    nextLeaf.setValues(values);
		}
		else { //node bean
		    BooleanQuestionNodeBean nextRealNode = (BooleanQuestionNodeBean)nextNode;
		    processNode(bqf, nextRealNode);
		}
	    }
	    answer = booleanAnswerPaging(request, rootNode);
	}

	request.getSession().setAttribute(CConstants.WDK_ANSWER_KEY, answer);
	request.getSession().setAttribute(CConstants.BOOLEAN_QUESTION_FORM_KEY, bqf);

	//add UserAnswerBean to UserAnswer for query history
    UserBean wdkUser = (UserBean)request.getSession().getAttribute(CConstants.WDK_USER_KEY);
    wdkUser.createHistory(answer);
	//set the question form bean saved at ShowQuestionAction time to be non-validating
	//to prevent problem at show history time (o.w. ShowSummaryAction goes to question page)
	QuestionForm qForm = (QuestionForm)request.getSession().getAttribute(CConstants.QUESTIONFORM_KEY);
	qForm.setNonValidating();

	/*DEBUG
	  System.err.println("DEBUG GBAA: reset cursor on boolean answer before listing");
	  answer.resetAnswerRowCursor();
	  java.util.Iterator it = answer.getRecords();
	  while (it.hasNext()) {
	  System.err.println("DEBUG GBAA: record is: " + (org.gusdb.wdk.model.jspwrap.RecordBean)it.next());
	  }
	  System.err.println("DEBUG GBAA: reset cursor on boolean answer after listing");
	  answer.resetAnswerRowCursor();
	*/
	
	ActionForward forward = mapping.findForward(CConstants.GET_BOOLEAN_ANSWER_MAPKEY);
	return forward;
    }


    private void processNode(BooleanQuestionForm bqf, BooleanQuestionNodeBean node){
        LinkedHashMap<String, String> values = new LinkedHashMap<String, String>();
	String opInternalName = node.getOperation();
	
	//	String value = (String)bqf.getMyProps().get(opDisplayName);
	values.put(BooleanQuery.OPERATION_PARAM_NAME, opInternalName);
	node.setValues(values);
    }

    static LinkedHashMap getParamsFromForm(BooleanQuestionForm bqf,
            BooleanQuestionLeafBean leaf) {

        Integer leafId = leaf.getLeafId();
        String leafPrefix = leafId.toString() + "_";
        QuestionBean question = leaf.getQuestion();
        ParamBean params[] = question.getParams();
        LinkedHashMap<String, String> values = new LinkedHashMap<String, String>();
        for (int i = 0; i < params.length; i++) {
            ParamBean nextParam = params[i];
            String formParamName = leafPrefix + nextParam.getName();
            Object nextValObj = bqf.getMyProps().get(formParamName);
            String nextValue;
            if (nextValObj instanceof String[]) {
                String[] vals = (String[]) nextValObj;
                nextValue = vals[0];
                for (int j = 1; j < vals.length; j++) {
                    nextValue += "," + vals[j];
                }
            } else {
                nextValue = (String) nextValObj;
            }
            values.put(nextParam.getName(), nextValue);
        }
        return values;
    }
}
