package org.gusdb.wdk.model;

import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.BooleanQuery;
import java.util.Hashtable;

/**
 * Represents a Question in boolean context.  A boolean Question is defined as
 * such by having a BooleanQuery as its Query.  Can take one of two forms:
 *
 * 1.  Representing a boolean Question and its two boolean operand Questions. 
 * The operands can also be boolean Questions so using this class one can create
 * a large tree of recursive boolean Questions. 
 * 
 * 2. Representing a normal Question that is not itself boolean but is the operand
 * for its parent boolean Question.  This is a leaf Question in a boolean Question
 * tree and its pointers to boolean operands are null values.
 * 
 * Also recursively sets operand Answers as parameter values for boolean Questions. 
 *
 * Created: Fri 22 October 12:00:00 2004 EST
 * 
 * @author David Barkan
 * @version $Revision$ $Date$Author:  $ 
 */

public class BooleanQuestionNode{

    // ------------------------------------------------------------------
    // Instance variables
    // ------------------------------------------------------------------
 
    /**
     * Question for which this BooleanQuestion node is a wrapper.
     */
    private Question question;

    /**
     * First operand Question; null if <code>question</code> is not a boolean
     * Question.
     */
    private BooleanQuestionNode firstChild;

    /**
     * Second operand Question; null if <code>question</code> is not a boolean
     * Question.
     */
    private BooleanQuestionNode secondChild;

    /**
     * Values which will be set for this Questions parameters.  These must be
     * instantiated for every node in this BooleanQuestionNode's tree before 
     * <code>setAllValues</code> is called, the exception being any values
     * for Answer parameters.  
     */
    private Hashtable values;

    
    // ------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------

    /**
     * Constructor for a BooleanQuestionNode representing a boolean Question.
     */
    public BooleanQuestionNode(Question q, BooleanQuestionNode firstChild, BooleanQuestionNode secondChild){
	this.question = q;
	this.firstChild = firstChild;
	this.secondChild = secondChild;
    }

    /**
     * Constructor for a BooleanQuestionNode representing a leaf in a boolean Query 
     * tree containing a Question that is not boolean.
     */
  
    public BooleanQuestionNode(Question q){
	this.question = q;
	this.firstChild = null;
	this.secondChild = null;
    }

    // ------------------------------------------------------------------
    // Public methods
    // ------------------------------------------------------------------

    public Question getQuestion(){
	return question;
    }

    public BooleanQuestionNode getFirstChild(){
	return firstChild;
    }

    public BooleanQuestionNode getSecondChild(){
	return secondChild;
    }

    /**
     * Recursive method that traverses <code>bqn</code> and sets its values,
     * which may be either normal query values if the node is a leaf or the 
     * Answers of its operands if the node is a boolean Question.  The method is
     * recursively called on each of the operands if the node is a boolean 
     * Question.
     * 
     * @param bqn The root of the tree on which to set answers and values; might be
     * the root of a subtree of a tree from a previous recursive call.
     *
     * @return the Answer of <code>bqn</code>.  The answer should not be used as the
     * answer returned by the top (recursive initializer) node; that should be 
     * retrieved by calling makeAnswer() on that node's Question after running
     * this method.
     */
    public static Answer setAllValues(BooleanQuestionNode bqn) throws WdkUserException, WdkModelException {
	
	//dtb -- initially this method was in BooleanQueryTester but I figured it might be needed by other classes
	
	Answer answer = null;

	if (bqn.isLeaf()){

	    Question leafQuestion = bqn.getQuestion();
	    Hashtable leafValues = bqn.getValues();
	    answer = leafQuestion.makeAnswer(leafValues, 0, 0);
	}
	else{  //bqn is boolean question

	    Question booleanQuestion = bqn.getQuestion();
	    
	    BooleanQuestionNode firstChild = bqn.getFirstChild();
	    Answer firstChildAnswer = BooleanQuestionNode.setAllValues(firstChild);

	    BooleanQuestionNode secondChild = bqn.getSecondChild();
	    Answer secondChildAnswer = BooleanQuestionNode.setAllValues(secondChild);
	    
	    Hashtable booleanValues = bqn.getValues();

	    booleanValues.put(BooleanQuery.FIRST_ANSWER_PARAM_NAME, firstChildAnswer);
	    booleanValues.put(BooleanQuery.SECOND_ANSWER_PARAM_NAME, secondChildAnswer);
    
	    answer = booleanQuestion.makeAnswer(booleanValues, 0, 0);
	}
	return answer;

    }
    
    /**
     * @return whether the node is a leaf in a boolean Question tree; that
     * is, if it is a normal Question without a Boolean Query as its Query.
     */

    public boolean isLeaf(){
	if (firstChild == null){
	    return true;
	}
	return false;
    }
    
    public void setValues(Hashtable values){
	this.values = values;
    }

    public Hashtable getValues(){
	return values;
    }

}
