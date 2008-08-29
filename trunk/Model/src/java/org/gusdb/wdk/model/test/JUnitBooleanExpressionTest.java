/**
 * 
 */
package org.gusdb.wdk.model.test;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.BooleanExpression;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.QuestionSet;
import org.gusdb.wdk.model.Reference;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;

/**
 * @author Jerric
 * @created Sep 14, 2005
 */
public class JUnitBooleanExpressionTest extends TestCase {

    private TestUtility utility;
    private WdkModel wdkModel;
    private SanityModel sanityModel;
    private Map<String, Answer> operandMap;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(JUnitBooleanExpressionTest.class);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new JUnitBooleanExpressionTest("testParseExpression"));
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
            throws WdkModelException, WdkUserException,
            NoSuchAlgorithmException, SQLException, JSONException {
        // get sanity questions
        SanityQuestion[] sqs = sanityModel.getAllSanityQuestions();

        assertTrue(sqs.length > 0);

        int idIndex = 0;
        // get a set of SanityQuestion with same type
        Map<String, Set<SanityQuestion>> sqSets = new LinkedHashMap<String, Set<SanityQuestion>>();
        Set<SanityQuestion> sqSet = null;
        for (SanityQuestion sq : sqs) {
            // get model question from sanity question
            Reference questionRef = new Reference(sq.getRef());
            QuestionSet questionSet = wdkModel.getQuestionSet(questionRef.getSetName());
            Question question = questionSet.getQuestion(questionRef.getElementName());
            String type = question.getRecordClass().getFullName();

            if (!sqSets.containsKey(type))
                sqSets.put(type, new LinkedHashSet<SanityQuestion>());
            sqSet = sqSets.get(type);
            sqSet.add(sq);
        }

        // create Answer list using the biggest sanity question set
        int max = sqSet.size();
        for (Set<SanityQuestion> subSet : sqSets.values()) {
            if (subSet.size() > max) {
                sqSet = subSet;
                max = subSet.size();
            }
        }

        // create answers for the biggest sanity question set
        operandMap = new LinkedHashMap<String, Answer>();
        for (SanityQuestion sq : sqSet) {
            // get model question from sanity question
            Reference questionRef = new Reference(sq.getRef());
            QuestionSet questionSet = wdkModel.getQuestionSet(questionRef.getSetName());
            Question question = questionSet.getQuestion(questionRef.getElementName());

            // run question
            Answer answer = question.makeAnswer(sq.getParamHash(),
                    sq.getPageStart(), sq.getPageEnd());
            // compose answer id and name
            idIndex++;
            String answerID = Integer.toString(idIndex);
            String answerName = ((idIndex % 2 == 1) ? "ans_" : "ans (");
            answerName += idIndex;
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
    public void testParseExpression() throws WdkUserException, WdkModelException {
        User user = wdkModel.getUserFactory().createGuestUser();
        BooleanExpression be = new BooleanExpression(user);

        String[] valid = { "1 OR 2", "1 AND (2 NOT 3)",
                "(1 AND 3)AND(2 AND 4)", "(1) OR (3)", "ans_1 OR \"ans (2\"" };
        // String[] valid = { "#1 OR #2"};

        for (String expression : valid) {
            try {
                Answer answer = be.parseExpression(expression);

                assertTrue(answer.getResultSize() >= 0);

                // TEST
                System.out.println(answer.printAsTable());
            } catch (Exception ex) {
                ex.printStackTrace();
                // System.err.println(ex);
                assertTrue(false);
            }
        }

        String[] invalid = { "#0 OR #2", "#1 AND (#2 NOT #3", "(#1 AND) #3",
                "#1(OR #2)", "#1 BAD #2", "ans_0 OR #1",
                "\"ans_1 OR \"ans (2\"" };

        for (String expression : invalid) {
            try {
                Answer answer = be.parseExpression(expression);

                assertNotNull(answer);
            } catch (Exception ex) {
                // TODO Auto-generated catch block
                // ex.printStackTrace();
                System.err.println(ex + " - expected.");
                assertTrue(true);
            }
        }
    }
}
