package org.gusdb.wdk.controller.action;

import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.jspwrap.BooleanQuestionLeafBean;
import org.gusdb.wdk.model.jspwrap.EnumParamBean;
import org.gusdb.wdk.model.jspwrap.FlatVocabParamBean;
import org.gusdb.wdk.model.jspwrap.ParamBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;


public class StartBooleanAction extends Action{

        public ActionForward execute(ActionMapping mapping,
				 ActionForm form,
				 HttpServletRequest request,
				 HttpServletResponse response) throws Exception {

	    BooleanQuestionForm bqf = new BooleanQuestionForm();
	    bqf.setMyProps(new LinkedHashMap());
	    bqf.setMyLabels(new LinkedHashMap());
	    bqf.setMyValues(new LinkedHashMap());
	    ActionServlet servlet = getServlet();
	    bqf.setServlet(servlet);
	
	    WdkModelBean wdkModel = (WdkModelBean)getServlet().getServletContext().getAttribute(CConstants.WDK_MODEL_KEY);
	    //change this so that it is getting it from the Leaf
	    EnumParamBean booleanOps = wdkModel.getBooleanOps();
	    String[] booleanOpsVocab = booleanOps.getVocab();
	    String[] booleanOpsVocabInternal = booleanOps.getVocabInternal();

	    bqf.getMyLabels().put(CConstants.BOOLEAN_OPERATIONS_PARAM_NAME, booleanOpsVocab);
	    bqf.getMyValues().put(CConstants.BOOLEAN_OPERATIONS_PARAM_NAME, booleanOpsVocabInternal);
	    
	    //get seed question, make into root of new tree
	    QuestionBean wdkQuestion = (QuestionBean)request.getSession().getAttribute(CConstants.WDK_QUESTION_KEY);
	    BooleanQuestionLeafBean leaf = wdkQuestion.makeBooleanQuestionLeaf();
	    addNewQuestionParams(bqf, leaf);

	    //request.getSession().setAttribute(CConstants.BOOLEAN_SEED_QUESTION_KEY, wdkQuestion);
	    request.getSession().setAttribute(CConstants.CURRENT_BOOLEAN_ROOT_KEY, leaf);
	    request.getSession().setAttribute(CConstants.BOOLEAN_QUESTION_FORM_KEY, bqf);

    	    ActionForward forward = mapping.findForward(CConstants.GROW_BOOLEAN_MAPKEY);
	    return forward;
	}
    
    
    protected void addNewQuestionParams(BooleanQuestionForm bqf, BooleanQuestionLeafBean leaf){

	Integer leafPrefixInt = leaf.getLeafId();
	String leafPrefix = leafPrefixInt.toString() + "_";
	
	QuestionBean leafQuestion = leaf.getQuestion();
	ParamBean[] params = leafQuestion.getParams();

	for (int i=0; i<params.length; i++) {
	    ParamBean p = params[i];
	    if (p instanceof FlatVocabParamBean) {
		//not assuming fixed order, so call once, use twice.
		String[] flatVocab = ((FlatVocabParamBean)p).getVocab();
		bqf.getMyValues().put(leafPrefix + p.getName(), flatVocab);
		bqf.getMyLabels().put(leafPrefix +  p.getName(), ShowQuestionAction.getLengthBoundedLabels(flatVocab));
	    }
	    bqf.getMyProps().put(leafPrefix + p.getName(), p.getDefault());
	}
    }
}
