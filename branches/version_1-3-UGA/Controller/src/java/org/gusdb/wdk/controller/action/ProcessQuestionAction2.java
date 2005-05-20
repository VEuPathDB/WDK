package org.gusdb.wdk.controller.action;

import java.util.*;
import java.util.Vector;
import java.util.Hashtable;
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
/* added statements */ 
import javax.servlet.http.HttpServlet;
import org.gusdb.wdk.model.jspwrap.QuestionSetBean;
import org.gusdb.wdk.model.jspwrap.ParamBean;
import org.gusdb.wdk.model.jspwrap.FlatVocabParamBean;
import org.gusdb.wdk.model.WdkModelException;

import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.jspwrap.EnumParamBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.AnswerBean;
import org.gusdb.wdk.model.jspwrap.BooleanQuestionLeafBean;
import org.gusdb.wdk.model.jspwrap.BooleanQuestionNodeBean;
import org.gusdb.wdk.model.BooleanQuery;




/**
 * This Action is called by the ActionServlet when a WDK question is asked.
 * It 1) reads param values from input form bean,
 *    2) runs the query and saves the answer
 *    3) forwards control to a jsp page that displays a summary
 */

public class ProcessQuestionAction2 extends Action {
    public ActionForward execute(ActionMapping mapping,
				 ActionForm form,
				 HttpServletRequest request,
				 HttpServletResponse response) throws Exception {



	QuestionSetForm qsf = (QuestionSetForm) form;

        WdkModelBean wdkModel = (WdkModelBean)getServlet().getServletContext().getAttribute(CConstants.WDK_MODEL_KEY);

        String qSetName = qsf.getQSetName();
        String qName = qsf.getQName();

        QuestionSetBean wdkQuestionSet = (QuestionSetBean)wdkModel.getQuestionSetsMap().get(qSetName);
        QuestionBean wdkQuestion = (QuestionBean)wdkQuestionSet.getQuestionsMap().get(qName);

	QuestionForm qForm = new QuestionForm();

	ActionServlet servlet = getServlet();
	
	ActionForward forward = null;

	String  submitAction = request.getParameter(CConstants.PQ_SUBMIT_KEY);

  
  if (submitAction.equals(CConstants.PQ_SUBMIT_GET_ANSWER)){
	qForm.setMyProps(new HashMap());
	qForm.setMyLabels(new HashMap());
	qForm.setMyValues(new HashMap());

      //The following works when a question has at most 1 non-organism param
      //We will need to loop throuqh all possible parameters in the future
	String [] vals = qsf.getValue();
	String k = qsf.getKey();
      if(k != null){	
	System.out.println("PQA2: setting key="+ k +" with "+ vals.length+" vals.  They are:");
   	  for(int i=0; i<vals.length; i++)
		System.out.print("   "+ vals[i]);

	  System.out.println(" ");

	if(qsf.getIsMultiPick()){
		System.out.println("PQA2: isMultiPick");
		qForm.getMyProps().put(k, vals);
	}else{
		System.out.println("PQA2: not isMultiPick");
		qForm.getMyProps().put(k, vals[0]);
	}
      }//end if k != null
	if(qsf.getNeedOrganism()){
	   qForm.getMyProps().put("organism", qsf.getOrganism());
	}


        request.getSession().setAttribute(CConstants.WDK_QUESTION_KEY, wdkQuestion);

	qForm.setServlet(servlet);
	
	qForm.setQuestion(wdkQuestion);

        request.getSession().setAttribute(CConstants.QUESTIONFORM_KEY, qForm);
	
	forward = mapping.findForward(CConstants.PQ_SHOW_SUMMARY_MAPKEY);


	}//end if GET ANSWER

   /**************************SET UP BOOLEAN QUESTIONS***************************/
    // else if (submitAction.equals(CConstants.PBQ_SUBMIT_GET_BOOLEAN_ANSWER)){

	else{
  System.out.println("PQA2: get boolean answer");

	    BooleanQuestionForm bqf = new BooleanQuestionForm();
	    bqf.setServlet(servlet);

	bqf.setMyProps(new HashMap());
	bqf.setMyLabels(new HashMap());
	bqf.setMyValues(new HashMap());

	
	BooleanQuestionLeafBean root = wdkQuestion.makeBooleanQuestionLeaf();
	int rid = bqf.getNextId();
	Integer rootID = new Integer(rid);
	root.setLeafId(rid);	 

	String [] vals1 = qsf.getValue();
	String k1 =rootID.toString()+"_"+ qsf.getKey();
	
	System.out.println("PQA2: setting bool key1="+ k1 +" with "+ vals1.length+" vals.  They are:");
   	  for(int i=0; i<vals1.length; i++)
		System.out.print("   "+ vals1[i]);

	  System.out.println(" ");

	if(qsf.getIsMultiPick()){
		System.out.println("PQA2: isMultiPick");
		bqf.getMyProps().put(k1, vals1);
	}else{
		System.out.println("PQA2: not isMultiPick");
		bqf.getMyProps().put(k1, vals1[0]);
	}
	String [] org1 = qsf.getOrganism();
	 if(org1.length==1)	 	   
	   bqf.getMyProps().put(rootID.toString()+"_"+"organism", org1[0]);
	else{
	   bqf.getMyProps().put(rootID.toString()+"_"+"organism", org1[0]);
	   bqf.getMyProps().put(rootID.toString()+"_"+"organism", org1[1]);
	 }
	    request.getSession().setAttribute(CConstants.BOOLEAN_QUESTION_FORM_KEY, bqf);



     /***************Add Operation and second question***************/
        String qSetName2 = qsf.getQSetName2();
        String qName2 = qsf.getQName2();
	ShowSummaryAction show = new ShowSummaryAction();


        QuestionSetBean wdkQuestionSet2 = (QuestionSetBean)wdkModel.getQuestionSetsMap().get(qSetName2);
        QuestionBean wdkQuestion2 = (QuestionBean)wdkQuestionSet2.getQuestionsMap().get(qName2);


	BooleanQuestionLeafBean newLeaf = wdkQuestion2.makeBooleanQuestionLeaf();
	int nid = bqf.getNextId();
	Integer newLeafID =new Integer(nid);
	     
	newLeaf.setLeafId(nid);	 

	String [] vals2 = qsf.getValue2();
	String k2 = newLeafID.toString()+"_"+qsf.getKey2();



	System.out.println("PQA2: setting bool key2="+ k2 +" with "+ vals2.length+" vals.  They are:");
   	  for(int i=0; i<vals2.length; i++)
		System.out.print("   "+ vals2[i]);

	  System.out.println(" ");


	if(qsf.getIsMultiPick()){
		System.out.println("PQA2: boolean-q2  isMultiPick");
		bqf.getMyProps().put(k2, vals2);
	}else{
		System.out.println("PQA2: boolean-q2 not isMultiPick");
		bqf.getMyProps().put(k2, vals2[0]);
	}
	System.out.println("PQA2:  getorg2 ="+qsf.getOrganism2());

	String [] org2 = qsf.getOrganism2();
	 if(org2.length==1)	 	   
	   bqf.getMyProps().put(newLeafID.toString()+"_"+"organism", org2[0]);
	else{
	   bqf.getMyProps().put(newLeafID.toString()+"_"+"organism", org2[0]);
	   bqf.getMyProps().put(newLeafID.toString()+"_"+"organism", org2[1]);
	 }
	Set set = (bqf.getMyProps()).keySet();
	Iterator iter  = set.iterator();
		System.out.println("PQA2: values in myProps"  );
	while(iter.hasNext()){
	    String theKey=(String)iter.next();
		System.out.println(theKey +" = " +(bqf.getMyProps()).get(theKey)  );
	 }

	String operation = qsf.getBooleanOp();

	  //Object currentRoot = request.getSession().getAttribute(CConstants.CURRENT_BOOLEAN_ROOT_KEY);
	Object currentRoot = (Object) root;	

    //System.out.println("PQA2:  operation = "+ operation);

	    BooleanQuestionLeafBean currentLeafRoot = root;		
	    currentLeafRoot.grow(newLeaf, operation, wdkModel);
	    currentRoot =  currentLeafRoot.getParent();

    //System.out.println("PQA2:  currentRoot = "+ currentRoot);
   
	    request.getSession().setAttribute(CConstants.CURRENT_BOOLEAN_ROOT_KEY, currentRoot);

	   // addNewQuestionParams(bqf, newLeaf);

	AnswerBean answer;
	if (currentRoot instanceof BooleanQuestionLeafBean) {
	//changed from root to currentRoot
  //System.out.println("PQA2: root is a leaf");
	    BooleanQuestionLeafBean rootLeaf = (BooleanQuestionLeafBean)root;
	    QuestionBean leafQuestion = rootLeaf.getQuestion();
	    //Map qparams = getParamsFromForm(bqf, rootLeaf);
	    //answer = show.summaryPaging(request, leafQuestion, qparams);
	    answer = show.summaryPaging(request, leafQuestion, bqf.getMyProps());
	} else {

   //System.out.println("PQA2: root is a node");
	    BooleanQuestionNodeBean rootNode = (BooleanQuestionNodeBean)currentRoot;
	    Vector allNodes = new Vector();
	    allNodes = rootNode.getAllNodes(allNodes);

   //System.out.println("PQA2: allnodes size= "+allNodes.size());
	
	    for (int i = 0; i < allNodes.size(); i++){
		Object nextNode = allNodes.elementAt(i);
		if (nextNode instanceof BooleanQuestionLeafBean){

   //System.out.println("PQA2: is leaf - nextNode= "+ nextNode);
		
		    BooleanQuestionLeafBean nextLeaf = (BooleanQuestionLeafBean)nextNode;
		    Hashtable values = getParamsFromForm(bqf, nextLeaf);
		 
		    nextLeaf.setValues(values);
		}
		else { //node bean
   //System.out.println("PQA2:   nextNode is node ");
		    BooleanQuestionNodeBean nextRealNode = (BooleanQuestionNodeBean)nextNode;
		    processNode(bqf, nextRealNode);
		}
	    }
	    rootNode.setAllValues();
	    answer = show.booleanAnswerPaging(request, rootNode);
	}

	request.setAttribute(CConstants.WDK_ANSWER_KEY, answer);

            forward = mapping.findForward(CConstants.PBQ_GET_BOOLEAN_ANSWER_MAPKEY);
        }


	return forward;
    }//end execute



  //Don't think we need this function -- just sets up default params 
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
		bqf.getMyLabels().put(leafPrefix +  p.getName(), flatVocab);
		bqf.getMyValues().put(leafPrefix + p.getName(), flatVocab);
	    }
	    bqf.getMyProps().put(leafPrefix + p.getName(), p.getDefault());
	}
    }


    private void processNode(BooleanQuestionForm bqf, BooleanQuestionNodeBean node){
	Hashtable values = new Hashtable();
	String opInternalName = node.getOperation();
	
	//	String value = (String)bqf.getMyProps().get(opDisplayName);
	values.put(BooleanQuery.OPERATION_PARAM_NAME, opInternalName);
  //System.out.println("PQA2:  BooleanQuery.OPERATION_PARAM_NAME, opInternalName:  "  + BooleanQuery.OPERATION_PARAM_NAME+", "+opInternalName);

	node.setValues(values);
    }

    private Hashtable getParamsFromForm(BooleanQuestionForm bqf, BooleanQuestionLeafBean leaf){

	Integer leafId = leaf.getLeafId();
	String leafPrefix = leafId.toString() + "_";
	QuestionBean question = leaf.getQuestion();
	ParamBean params[] = question.getParams();
	Hashtable values = new Hashtable();
  //System.out.println("PQA2: param length= "+params.length);
	for (int i = 0; i < params.length; i++){
	    ParamBean nextParam = params[i];
  //System.out.println("PQA2: param = "+nextParam);

	    String formParamName = leafPrefix + nextParam.getName();
  //System.out.println("PQA2: param name= "+ formParamName);

	    String nextValue = (String)bqf.getMyProps().get(formParamName);
  //System.out.println("PQA2: nextVal = "+nextValue);
	    values.put(nextParam.getName(), nextValue);
	}
	return values;
    }



}//end ProcessQuestionAction2
