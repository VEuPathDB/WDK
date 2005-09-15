/**
 * 
 */
package org.gusdb.wdk.model.test;

import java.util.HashMap;
import java.util.Map;

import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.BooleanExpression;
import org.gusdb.wdk.model.BooleanQuestionNode;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.QuestionSet;
import org.gusdb.wdk.model.Reference;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Jerric
 * @created Sep 14, 2005
 */
public class JUnitBooleanExpressionTest extends TestCase {

    private TestUtility utility;
    private WdkModel wdkModel;
    private SanityModel sanityModel;
    Map<String, Answer> operandMap;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(JUnitBooleanExpressionTest.class);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new JUnitBooleanExpressionTest("testCombineAnswers"));
        return suite;
    }

    /**
     * 
     */
    public JUnitBooleanExpressionTest() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param name
     */
    public JUnitBooleanExpressionTest(String name) {
        super(name);
        // TODO Auto-generated constructor stub
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        utility = TestUtility.getInstance();
        wdkModel = utility.getWdkModel();
        sanityModel = utility.getSanityModel();

        operandMap = buildOperandMap(sanityModel);
    }

    private Map<String, Answer> buildOperandMap(SanityModel sanityModel)
            throws WdkModelException, WdkUserException {
        // get sanity questions
        SanityQuestion[] sqs = sanityModel.getAllSanityQuestions();

        assertTrue(sqs.length > 0);

        operandMap = new HashMap<String, Answer>();
        int idIndex = 0;
        for (SanityQuestion sq : sqs) {
            // get model question from sanity question
            Reference questionRef = new Reference(sq.getRef());
            QuestionSet questionSet = wdkModel.getQuestionSet(questionRef.getSetName());
            Question question = questionSet.getQuestion(questionRef.getElementName());

            // run question
            Answer answer = question.makeAnswer(sq.getParamHash(),
                    sq.getPageStart(), sq.getPageEnd());
            // compose answer id and name
            idIndex++;
            String answerID = "#" + idIndex;
            String answerName = "Test_Answer_" + idIndex;
            operandMap.put(answerID, answer);
            operandMap.put(answerName, answer);
        }
        return operandMap;
    }

    /*
     * Test method for
     * 'org.gusdb.wdk.model.BooleanExpression.combineAnswers(String, Map<String,
     * Answer>)'
     */
    public void testCombineAnswers() {
        BooleanExpression be = new BooleanExpression(wdkModel);

        String[] testCases = { "#1 UNION #2", "#1 INTERSECT (#2 MINUS #3)" };

        for (String expression : testCases) {
            try {
                BooleanQuestionNode bqn = be.combineAnswers(expression,
                        operandMap);

                assertNotNull(bqn);

                // make answer
                Answer answer = bqn.makeAnswer(1, 20);

                assertNotNull(answer);

                // System.out.println(answer.printAsTable());
            } catch (Exception ex) {
                // TODO Auto-generated catch block
                ex.printStackTrace();
                // System.err.println(ex);
                assertTrue(false);
            }
        }
    }
}
