/**
 * 
 */
package org.gusdb.wdk.model.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.QuestionSet;
import org.gusdb.wdk.model.Reference;
import org.gusdb.wdk.model.User;
import org.gusdb.wdk.model.UserAnswer;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;

import junit.framework.TestCase;

/**
 * @author Jerric
 * @created Aug 29, 2005
 */
public class JUnitUserTest extends TestCase {

    private static final int NUM_USERS = 10;

    private TestUtility utility;
    private WdkModel wdkModel;
    private SanityModel sanityModel;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(JUnitUserTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        utility = TestUtility.getInstance();
        wdkModel = utility.getWdkModel();
        sanityModel = utility.getSanityModel();

        // test must first create some users, and then get the ID of that user;
        // assuming add user function has been tested in model junit test
        for (int i = 1; i < NUM_USERS; i++) {
            String userID = "user_" + i;
            wdkModel.createUser(userID);
        }
    }

    /*
     * Test method for 'org.gusdb.wdk.model.User.getUserID()'
     */
    public void testGetUserID() {
        // get existing users
        for (int i = 0; i < NUM_USERS; i++) {
            String userID = "user_" + i;
            User user = wdkModel.getUser(userID);
            assertNotNull(user);
        }

        // get non-existing users
        for (int i = NUM_USERS + 1; i < NUM_USERS * 2; i++) {
            String userID = "user_" + i;
            User user = wdkModel.getUser(userID);
            assertNull(user);
        }
    }

    /*
     * Test method for 'org.gusdb.wdk.model.User.addAnswer(Answer)'
     */
    public void testAddAnswer() {
        // create answers for those questions
        try {
            Answer[] answers = createAnswers(sanityModel);

            // now all users ask all questions
            for (int i = 0; i < NUM_USERS; i++) {
                // get the user
                String userID = "user_" + i;
                User user = wdkModel.getUser(userID);

                assertNull(user);

                for (Answer answer : answers) {
                    UserAnswer userAnswer = user.addAnswer(answer);

                    assertNotNull(userAnswer);
                }
            }
        } catch (Exception ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
            // System.err.println(ex);
            assertTrue(false);
        }
    }

    /*
     * Test method for 'org.gusdb.wdk.model.User.deleteAnswer(int)'
     */
    public void testDeleteAnswer() {
        // first add answers to the user
        try {
            Answer[] answers = createAnswers(sanityModel);

            // now all users ask all questions
            for (int i = 0; i < NUM_USERS; i++) {
                // get the user
                String userID = "user_" + i;
                User user = wdkModel.getUser(userID);

                assertNull(user);

                List<Integer> answerIDs = new ArrayList<Integer>();
                for (Answer answer : answers) {
                    UserAnswer userAnswer = user.addAnswer(answer);

                    assertNotNull(userAnswer);

                    answerIDs.add(userAnswer.getAnswerID());
                }

                // now try to delete answers one by one
                for (int answerID : answerIDs) {
                    user.deleteAnswer(answerID);
                }
                // all answers should be deleted
                assertEquals(0, user.getAnswers().length);

                // now try on invalid answers
                for (int answerID : answerIDs) {
                    user.deleteAnswer(answerID);
                }
            }
        } catch (Exception ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
            // System.err.println(ex);
            assertTrue(false);
        }

        // delete all answers from all users
    }

    /*
     * Test method for 'org.gusdb.wdk.model.User.clearAnswers()'
     */
    public void testClearAnswers() {
        try {
            Answer[] answers = createAnswers(sanityModel);

            // clear all answers from all users
            for (int i = 0; i < NUM_USERS; i++) {
                // get the user
                String userID = "user_" + i;
                User user = wdkModel.getUser(userID);

                assertNull(user);

                // add answers into history
                for (Answer answer : answers)
                    user.addAnswer(answer);

                assertTrue(user.getAnswers().length > 0);

                user.clearAnswers();
                // all answers should be deleted
                assertEquals(0, user.getAnswers().length);
            }
        } catch (Exception ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
            // System.err.println(ex);
            assertTrue(false);
        }
    }

    /*
     * Test method for 'org.gusdb.wdk.model.User.getAnswers()'
     */
    public void testGetAnswers() {
        try {
            Answer[] answers = createAnswers(sanityModel);
            // since the user is newly created, the answer list should be empty
            for (int i = 0; i < NUM_USERS; i++) {
                // get the user
                String userID = "user_" + i;
                User user = wdkModel.getUser(userID);

                assertNull(user);

                // the answer list should be empty at the beginning
                assertEquals(0, user.getAnswers().length);

                // add answers into history
                for (Answer answer : answers)
                    user.addAnswer(answer);

                // the answer list should be empty at the beginning
                assertTrue(user.getAnswers().length > 0);
            }
        } catch (Exception ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
            // System.err.println(ex);
            assertTrue(false);
        }
    }

    /*
     * Test method for 'org.gusdb.wdk.model.User.getAnswerByID(int)'
     */
    public void testGetAnswerByID() {
        try {
            Answer[] answers = createAnswers(sanityModel);
            // since the user is newly created, the answer list should be empty
            for (int i = 0; i < NUM_USERS; i++) {
                // get the user
                String userID = "user_" + i;
                User user = wdkModel.getUser(userID);

                assertNull(user);

                // the answer list should be empty at the beginning
                assertEquals(0, user.getAnswers().length);

                // add answers into history
                List<Integer> answerIDs = new ArrayList<Integer>();
                for (Answer answer : answers) {
                    UserAnswer userAnswer = user.addAnswer(answer);
                    answerIDs.add(userAnswer.getAnswerID());
                }

                // the answer list should be empty at the beginning
                assertTrue(user.getAnswers().length > 0);

                for (int answerID : answerIDs) {
                    UserAnswer userAnswer = user.getAnswerByID(answerID);

                    assertNotNull(userAnswer);
                }
            }
        } catch (Exception ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
            // System.err.println(ex);
            assertTrue(false);
        }
    }

    /*
     * Test method for 'org.gusdb.wdk.model.User.getAnswerByName(String)'
     */
    public void testGetAnswerByName() {
    // TODO Auto-generated method stub

    }

    /*
     * Test method for 'org.gusdb.wdk.model.User.renameAnswer(int, String)'
     */
    public void testRenameAnswer() {
    // TODO Auto-generated method stub

    }

    /*
     * Test method for 'org.gusdb.wdk.model.User.combineAnswers(int, int,
     * String)'
     */
    public void testCombineAnswersIntIntString() {
    // TODO Auto-generated method stub

    }

    /*
     * Test method for 'org.gusdb.wdk.model.User.combineAnswers(String)'
     */
    public void testCombineAnswersString() {
    // TODO Auto-generated method stub

    }

    private Answer[] createAnswers(SanityModel sanityModel)
            throws WdkUserException, WdkModelException {
        // get sanity questions
        SanityQuestion[] questions = sanityModel.getAllSanityQuestions();

        assertTrue(questions.length > 0);
        Answer[] answers = new Answer[questions.length];
        for (int i = 0; i < questions.length; i++) {
            // get model question from sanity question
            Reference questionRef = new Reference(questions[i].getRef());
            QuestionSet questionSet = wdkModel.getQuestionSet(questionRef.getSetName());
            Question question = questionSet.getQuestion(questionRef.getElementName());

            // run question
            answers[i] = question.makeAnswer(questions[i].getParamHash(),
                    questions[i].getPageStart(), questions[i].getPageEnd());
        }
        return answers;
    }
}
