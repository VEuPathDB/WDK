package org.gusdb.wdk.model.test;

import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.Reference;
import org.gusdb.wdk.model.BooleanQuery;

import org.gusdb.wdk.model.BooleanQuestionNode;

import java.util.Hashtable;


/**
 * TestBooleanTree.java
 *
 * Class with static method that returns a tree of BooleanQuestionNodes
 * containing Questions and parameter values specifically designed for
 * the toy model.
 *
 * Tree is composed entirely of the RnaQuestions.ByNumSeqs Questions and
 * looks like this:
 *
 *       q1 subtract
 *       /     \
 *    q2union  q3union
 *    /  \     / \
 *   q4   q5  q6  q7   leaf nodes
 *
 * (Update this picture if changing any relevant data below).
 *
 * Created: Fri 22 October 12:00:00 2004 EST
 * 
 * @author David Barkan
 * @version $Revision$ $Date$Author:  $ 
 */

public class TestBooleanTree {

    // ------------------------------------------------------------------
    // Static method
    // ------------------------------------------------------------------
    
    /**
     * @param model WdkModel representing the Toy Model.
     *
     * @return Root of tree described above with six other BooleanQuestionNodes
     * in its trees.
     */

    static BooleanQuestionNode getTestTree(WdkModel model)throws WdkModelException, WdkUserException{

	//leaf nodes
	Question q4 = makeNumSeqsQuestion(model);
	Hashtable q4values = makeNumSeqsValues("5", "Eimeria tenella");
	
	Question q5 = makeNumSeqsQuestion(model);
	Hashtable q5values = makeNumSeqsValues("5", "Neospora caninum");

	Question q6 = makeNumSeqsQuestion(model);
	Hashtable q6values = makeNumSeqsValues("25", "Eimeria tenella");

	//	Question q7 = makeNumSeqsQuestion(model);
	//Hashtable q7values = makeNumSeqsValues("25", "Neospora caninum");
	
	Reference numSeqsRef = new Reference("RnaQuestions.ByDbESTLib");
	Question q7 = model.getQuestionSet(numSeqsRef.getSetName()).getQuestion(numSeqsRef.getElementName());
	Hashtable q7values = new Hashtable();
	q7values.put("NumEstLibs", "5");
	q7values.put("ApiTaxon", "Neospora caninum");
	

	//boolean nodes
	Question q2 = makeBooleanQuestion(model, q7.getRecordClass());
	Hashtable q2values = makeBooleanValues("Union");

	Question q3 = makeBooleanQuestion(model, q7.getRecordClass());
	Hashtable q3values = makeBooleanValues("Union");

	//root boolean node
	Question q1 = makeBooleanQuestion(model, q7.getRecordClass());
	Hashtable q1values = makeBooleanValues("Minus");

	BooleanQuestionNode bqn4 = new BooleanQuestionNode(q4);
	BooleanQuestionNode bqn5 = new BooleanQuestionNode(q5);
	BooleanQuestionNode bqn6 = new BooleanQuestionNode(q6);
	BooleanQuestionNode bqn7 = new BooleanQuestionNode(q7);

	BooleanQuestionNode bqn3 = new BooleanQuestionNode(q3, bqn6, bqn7);
	BooleanQuestionNode bqn2 = new BooleanQuestionNode(q2, bqn4, bqn5);
	BooleanQuestionNode bqn1 = new BooleanQuestionNode(q1, bqn2, bqn3);
	
	bqn1.setValues(q1values);
	bqn2.setValues(q2values);
	bqn3.setValues(q3values);
	bqn4.setValues(q4values);
	bqn5.setValues(q5values);
	bqn6.setValues(q6values);
	bqn7.setValues(q7values);
	
	return bqn1;
    }

    // ------------------------------------------------------------------
    // Private Methods
    // ------------------------------------------------------------------

    private static Hashtable makeBooleanValues(String operation){
	Hashtable h = new Hashtable();
	h.put(BooleanQuery.OPERATION_PARAM_NAME, operation);
	return h;
    }

    private static Question makeBooleanQuestion(WdkModel model, RecordClass rc){
	
	Question q = new Question();
	q.setName("BooleanQuestion");
	q.setRecordClass(rc);
	BooleanQuery bq = model.makeBooleanQuery();
	
	q.setQuery(bq);
	return q;
    }

    private static Question makeNumSeqsQuestion(WdkModel model) throws WdkUserException, WdkModelException{

	Reference numSeqsRef = new Reference("RnaQuestions.ByNumSeqs");
	Question q = model.getQuestionSet(numSeqsRef.getSetName()).getQuestion(numSeqsRef.getElementName());
	return q;	
    }
    
    private static Hashtable makeNumSeqsValues(String numSeqs, String taxonName){
	Hashtable values = new Hashtable();
	values.put("NumSeqs", numSeqs);
	values.put("ApiTaxon", taxonName);
	return values;
    }
    





}

