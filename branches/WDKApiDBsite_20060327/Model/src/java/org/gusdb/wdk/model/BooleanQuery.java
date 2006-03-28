package org.gusdb.wdk.model;



/**
 * BooleanQuery.java
 *
 * A Query representing the pairing of two other Queries (known as 'boolean operands')
 * in a boolean operation (Union, Intersect, Subtract, etc.).  The expected use of a 
 * BooleanQuery is as the ID Query for a Question and thus a BooleanQuery is fundamentally
 * tied to other Questions and their Answers.  A BooleanQuery has three parameters.  
 * Two are AnswerParams, representing Answers to Questions whose Queries are the boolean
 * operands.  The Questions must have the same RecordClasses in order to be operands.  
 * The third is the operation to be performed which is a StringParam.  
 * BooleanQueries are recursive, and thus the operand Queries can themselves be BooleanQueries. 
 
 * BooleanQueries are used like any other Query by making BooleanQueryInstances; the result
 * is the result of the two operand Queries joined by the operation.  BooleanQueries differ
 * from other Queries in that a different one should be used every time a BooleanQuery is run
 * (rather than the normal use of one Query providing many QueryInstances).
 *
 * Queries need to declare their columns, so a BooleanQuery provides this, but only when
 * a BooleanQueryInstance has been created and its AnswerParameters have been set.  The columns
 * of the BooleanQuery then become the Columns of the ID Queries in the Answer's Question.
 * The two Query operands in a BooleanQuery must have the same declared columns.  
 *
 * It is the responsibility of whoever creates a BooleanQuery to set its RDBMSPlatform and
 * ResultFactory (this differs from other Queries whose resources are set by the WdkModel
 * upon instantiation).
 *
 * Created: Fri May 21 1821:30 EDT 2004
 *
 * @author David Barkan
 * @version $Revision$ $Date: 2006-02-20 14:40:26 -0500 (Mon, 20 Feb 2006) $ $Author$
 */

public class BooleanQuery extends Query {

    // ------------------------------------------------------------------
    // Static Variables
    // ------------------------------------------------------------------

    /**
     * Name of the AnswerParam whose value is the first Answer operand in the BooleanQuery.
     */
    public static final String FIRST_ANSWER_PARAM_NAME = "firstAnswer";

    /**
     * Name of the AnswerParam whose value is the second Answer operand in the BooleanQuery.
     */
    public static final String SECOND_ANSWER_PARAM_NAME = "secondAnswer";

    /**
     * Name of the StringParam whose value is the operation in this BooleanQuery.
     */
    //DTB -- we should make this come from a controlled vocabulary
    public static final String OPERATION_PARAM_NAME = "operation";

    /**
     * ParamSet for all boolean parameters.
     */
    public static final String BOOLEAN_PARAM_SET_NAME = "booleanParamSet";
    
    // ------------------------------------------------------------------
    // Instance Variables
    // ------------------------------------------------------------------

    protected RDBMSPlatformI platform;
    protected ResultFactory resultFactory;


    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------
    
    /**
     * Normal constructor for a BooleanQuery; handles parameter creation.
     */
    public BooleanQuery(){
	setName("BooleanQuery");
	setSetName("BooleanQuerySet");

	//DTB -- need to figure out what to have for prompt, help, etc.
	AnswerParam firstParam = makeAnswerParam(FIRST_ANSWER_PARAM_NAME, "q1 prompt", "q1 help", "q1 default");
	addParam(firstParam);
	
	AnswerParam secondParam = makeAnswerParam(SECOND_ANSWER_PARAM_NAME, "q2 prompt", "q2 help", "q2 default");
	addParam(secondParam);
	
	StringParam operation = new StringParam();
	operation.setName(OPERATION_PARAM_NAME);
	operation.setFullName(BOOLEAN_PARAM_SET_NAME);
	addParam(operation);
    }
    
    // ------------------------------------------------------------------
    // Public Methods
    // ------------------------------------------------------------------
    public RDBMSPlatformI getRDBMSPlatform(){
	return this.platform;
    }

    public ResultFactory getResultFactory(){
	return this.resultFactory;
    }
    
    // ------------------------------------------------------------------
    // Query
    // ------------------------------------------------------------------

    /**
     * @return BooleanQueryInstance on which one can set Answers and operations as
     * values.
     */
    public QueryInstance makeInstance(){
	return new BooleanQueryInstance(this);
    }

    // ------------------------------------------------------------------
    // Package Methods
    // ------------------------------------------------------------------

    void setRDBMSPlatform(RDBMSPlatformI platform){
	this.platform = platform;
    }
    

    void setResultFactory(ResultFactory rf){
	this.resultFactory = rf;
    }

        
    // ------------------------------------------------------------------
    // Private Methods
    // ------------------------------------------------------------------
    
    /**
     * Helper method for the constructor.
     */
    private AnswerParam makeAnswerParam(String name, String prompt, String help, String defaultValue){
	
	AnswerParam qp = new AnswerParam();
	qp.setName(name);
	qp.setFullName(BOOLEAN_PARAM_SET_NAME);
	qp.setPrompt(prompt);
	qp.setHelp(help);
	qp.setDefault(defaultValue);
	return qp;
    }

}
