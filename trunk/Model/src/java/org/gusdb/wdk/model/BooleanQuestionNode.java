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
 * @version $Revision$ $Date$Author: dbarkan $ 
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

    public void grow(BooleanQuestionNode bqn, String operation, WdkModel model ) throws WdkModelException{
	
	if (!isLeaf()){
	    throw new WdkModelException("Cannot grow an internal node; can only grow a leaf");
	}

	//make value copy of myself to be new left child
	BooleanQuestionNode newFirstChild = new BooleanQuestionNode(this.question);
	Hashtable valuesCopy = (Hashtable)values.clone();
	newFirstChild.setValues(valuesCopy);
	
	//set new left child
	this.firstChild = newFirstChild;

	//set bqn to be new right child
	this.secondChild = bqn;
	
	//transform myself into an internal BooleanQuestionNode
	this.values = null;
	this.values = new Hashtable();
	values.put(BooleanQuery.OPERATION_PARAM_NAME, operation);
	RecordClass rc = question.getRecordClass();
	this.question = model.makeBooleanQuestion(rc);
	

	//make new BooleanQuestionNode which is a copy of me and save it
	//make me into a real booleanQuestionNode--new BooleanQuery, etc.
	//assume that no Answers have been retrieved yet
	//My left child is what I used to be
	//right child is bqn
	//operation is op
	//acts on tree so no need to return anything

    }

    /**
     * Recursive method to find a node in the tree.
     *
     * @param nodeId Binary number representing path to take to find node.
     * The number is read left to right.  A 1 in the number will traverse to the left
     * child and a 0 in the number will traverse to the right.  When the end of the number
     * is reached, the current node is returned.
     *
     * @return the BooleanQuestionNode which is being sought.
     */
    public BooleanQuestionNode find(String nodeId) throws WdkModelException{
	String trimmedNodeId = null;
	BooleanQuestionNode nextChild = null;
	int nodeIdLength = nodeId.length();
	if (nodeId.equals("")){ //base case
	    return this;
	}
	else {
	    trimmedNodeId = nodeId.substring(1, nodeIdLength);
	    char nextChildPath = nodeId.charAt(0);
	    if (nextChildPath == '0'){
	        nextChild = firstChild; 
	    }
	    if (nextChildPath == '1'){
		nextChild = secondChild;
	    }
	    if (nextChildPath != '0' && nextChildPath != '1'){
		throw new WdkModelException("Path to find child in BooleanQuestionNode tree is not binary: " + nodeId);
	    }
	}
	
	return nextChild.find(trimmedNodeId);
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

    public String toString(){
	if (isLeaf()){
	    return ("Leaf node with parameter values: " + values.toString());
	}
	else {
	    StringBuffer sb = new StringBuffer("Internal Boolean node; operation: " + values.get(BooleanQuery.OPERATION_PARAM_NAME));
	    sb.append("\n\tFirst Child: " + firstChild.toString());
	    sb.append("\n\tSecond Child: " + secondChild.toString());
	    return sb.toString();
	}
    }
    
}


